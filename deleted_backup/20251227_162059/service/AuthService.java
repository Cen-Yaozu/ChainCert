package com.blockchain.certificate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blockchain.certificate.shared.exception.BusinessException;
import com.blockchain.certificate.model.dto.LoginRequest;
import com.blockchain.certificate.model.dto.RefreshTokenRequest;
import com.blockchain.certificate.domain.user.model.User;
import com.blockchain.certificate.model.vo.LoginResponse;
import com.blockchain.certificate.domain.user.repository.UserRepository;
import com.blockchain.certificate.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 * 处理登录、登出、令牌刷新等认证相关操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final CaptchaService captchaService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${system.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${system.account-lock-duration:1800000}")
    private long accountLockDuration;

    private static final String LOCK_PREFIX = "account:lock:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含令牌和用户信息）
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 验证验证码
        if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 查询用户
        User user = userRepository.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查账户锁定状态
        checkAccountLock(user);

        try {
            // 4. 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 5. 认证成功，重置失败计数
            resetFailedLoginCount(user);

            // 6. 更新最后登录时间
            userRepository.update(null,
                    new LambdaUpdateWrapper<User>()
                            .eq(User::getId, user.getId())
                            .set(User::getLastLoginTime, LocalDateTime.now())
            );

            // 7. 生成令牌
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            // 8. 存储刷新令牌到 Redis
            storeRefreshToken(user.getUsername(), refreshToken);

            // 9. 构建响应
            return LoginResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .userInfo(LoginResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .name(user.getName())
                            .role(user.getRole())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .collegeId(user.getCollegeId())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            // 密码错误，增加失败计数
            handleFailedLogin(user);
            throw new BusinessException("用户名或密码错误");
        } catch (LockedException e) {
            throw new BusinessException("账户已被锁定，请稍后再试");
        } catch (DisabledException e) {
            throw new BusinessException("账户已被禁用");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 新的访问令牌
     */
    public String refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. 验证刷新令牌
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        // 2. 从令牌中获取用户名
        String username = tokenProvider.getUsernameFromToken(refreshToken);

        // 3. 验证刷新令牌是否存在于 Redis
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException("刷新令牌无效");
        }

        // 4. 查询用户
        User user = userRepository.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        // 5. 生成新的访问令牌
        String rolesString = tokenProvider.getRolesFromToken(refreshToken);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList(rolesString)
        );

        return tokenProvider.generateAccessToken(authentication);
    }

    /**
     * 登出
     *
     * @param username 用户名
     */
    public void logout(String username) {
        // 删除 Redis 中的刷新令牌
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
        log.info("User logged out: {}", username);
    }

    /**
     * 检查账户锁定状态
     */
    private void checkAccountLock(User user) {
        // 检查数据库中的锁定状态
        if ("LOCKED".equals(user.getStatus())) {
            throw new BusinessException("账户已被管理员锁定");
        }

        // 检查 Redis 中的临时锁定
        String lockKey = LOCK_PREFIX + user.getUsername();
        String lockValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            throw new BusinessException("账户已被锁定，请30分钟后再试");
        }
    }

    /**
     * 处理登录失败
     */
    private void handleFailedLogin(User user) {
        int failedCount = user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount();
        failedCount++;

        // 更新失败次数
        userRepository.update(null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId, user.getId())
                        .set(User::getFailedLoginCount, failedCount)
        );

        // 如果达到最大失败次数，锁定账户
        if (failedCount >= maxLoginAttempts) {
            lockAccount(user.getUsername());
            throw new BusinessException("登录失败次数过多，账户已被锁定30分钟");
        }

        int remainingAttempts = maxLoginAttempts - failedCount;
        throw new BusinessException("用户名或密码错误，还有 " + remainingAttempts + " 次尝试机会");
    }

    /**
     * 锁定账户
     */
    private void lockAccount(String username) {
        String lockKey = LOCK_PREFIX + username;
        redisTemplate.opsForValue().set(
                lockKey,
                "locked",
                accountLockDuration,
                TimeUnit.MILLISECONDS
        );
        log.warn("Account locked due to too many failed login attempts: {}", username);
    }

    /**
     * 重置失败登录计数
     */
    private void resetFailedLoginCount(User user) {
        if (user.getFailedLoginCount() != null && user.getFailedLoginCount() > 0) {
            userRepository.update(null,
                    new LambdaUpdateWrapper<User>()
                            .eq(User::getId, user.getId())
                            .set(User::getFailedLoginCount, 0)
            );
        }
    }

    /**
     * 存储刷新令牌到 Redis
     */
    private void storeRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        long expiration = tokenProvider.getExpirationFromToken(refreshToken).getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, refreshToken, expiration, TimeUnit.MILLISECONDS);
    }
}
