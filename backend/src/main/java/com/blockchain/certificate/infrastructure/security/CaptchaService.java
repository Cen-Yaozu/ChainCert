package com.blockchain.certificate.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * 负责生成、存储和验证图形验证码
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;

    @Value("${system.captcha-expiration:300000}")
    private long captchaExpiration;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 40;
    private static final String CAPTCHA_CHARS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    /**
     * 生成验证码
     *
     * @return 验证码数据（包含 key 和 base64 图片）
     */
    public CaptchaResult generateCaptcha() {
        // 生成验证码文本
        String captchaText = generateCaptchaText();
        
        // 生成唯一 key
        String key = UUID.randomUUID().toString();
        
        // 存储到 Redis
        String redisKey = CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(
                redisKey,
                captchaText.toLowerCase(),
                captchaExpiration,
                TimeUnit.MILLISECONDS
        );
        
        // 生成图片
        String base64Image = generateCaptchaImage(captchaText);
        
        log.debug("Generated captcha with key: {}", key);
        
        return new CaptchaResult(key, base64Image);
    }

    /**
     * 验证验证码
     *
     * @param key     验证码 key
     * @param captcha 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean validateCaptcha(String key, String captcha) {
        if (key == null || captcha == null) {
            log.warn("Captcha validation failed: key or captcha is null");
            return false;
        }

        String redisKey = CAPTCHA_PREFIX + key;
        String storedCaptcha = redisTemplate.opsForValue().get(redisKey);

        if (storedCaptcha == null) {
            log.warn("Captcha validation failed: captcha not found or expired for key: {}", key);
            return false;
        }

        // 验证后删除验证码（一次性使用）
        redisTemplate.delete(redisKey);

        boolean isValid = storedCaptcha.equalsIgnoreCase(captcha.trim());
        
        if (!isValid) {
            log.warn("Captcha validation failed: incorrect captcha for key: {}", key);
        } else {
            log.debug("Captcha validation successful for key: {}", key);
        }

        return isValid;
    }

    /**
     * 生成验证码文本
     *
     * @return 验证码文本
     */
    private String generateCaptchaText() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = random.nextInt(CAPTCHA_CHARS.length());
            captcha.append(CAPTCHA_CHARS.charAt(index));
        }
        
        return captcha.toString();
    }

    /**
     * 生成验证码图片
     *
     * @param captchaText 验证码文本
     * @return Base64 编码的图片
     */
    private String generateCaptchaImage(String captchaText) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        Random random = new Random();

        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 绘制干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(IMAGE_WIDTH);
            int y1 = random.nextInt(IMAGE_HEIGHT);
            int x2 = random.nextInt(IMAGE_WIDTH);
            int y2 = random.nextInt(IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码字符
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int charWidth = IMAGE_WIDTH / CAPTCHA_LENGTH;
        
        for (int i = 0; i < captchaText.length(); i++) {
            // 随机颜色
            g.setColor(new Color(
                    random.nextInt(150),
                    random.nextInt(150),
                    random.nextInt(150)
            ));
            
            // 随机位置和角度
            int x = i * charWidth + 10 + random.nextInt(10);
            int y = 25 + random.nextInt(10);
            
            g.drawString(String.valueOf(captchaText.charAt(i)), x, y);
        }

        // 绘制干扰点
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(IMAGE_WIDTH);
            int y = random.nextInt(IMAGE_HEIGHT);
            g.setColor(new Color(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255)
            ));
            g.fillOval(x, y, 2, 2);
        }

        g.dispose();

        // 转换为 Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("Error generating captcha image", e);
            throw new RuntimeException("Failed to generate captcha image", e);
        }
    }

    /**
     * 验证码结果
     */
    public static class CaptchaResult {
        private final String key;
        private final String image;

        public CaptchaResult(String key, String image) {
            this.key = key;
            this.image = image;
        }

        public String getKey() {
            return key;
        }

        public String getImage() {
            return image;
        }
    }
}
