package com.blockchain.certificate.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider
 * 负责生成、验证和解析 JWT 令牌
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 生成访问令牌
     *
     * @param authentication 认证信息
     * @return JWT 访问令牌
     */
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtExpiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param authentication 认证信息
     * @return JWT 刷新令牌
     */
    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshExpiration);
    }

    /**
     * 生成令牌
     *
     * @param authentication 认证信息
     * @param expiration     过期时间（毫秒）
     * @return JWT 令牌
     */
    private String generateToken(Authentication authentication, long expiration) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // 获取用户角色
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 获取用户ID
        String userId = null;
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        }

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate);

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder.signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取角色
     *
     * @param token JWT 令牌
     * @return 角色字符串
     */
    public String getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roles", String.class);
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT 令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", String.class);
    }

    /**
     * 验证令牌
     *
     * @param token JWT 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT 令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            log.error("Error checking token expiration: {}", ex.getMessage());
            return true;
        }
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT 令牌
     * @return 过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 解析令牌
     *
     * @param token JWT 令牌
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
