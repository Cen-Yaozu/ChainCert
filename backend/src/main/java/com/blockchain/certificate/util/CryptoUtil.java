package com.blockchain.certificate.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 * 提供 AES 加密/解密功能，用于私钥安全存储
 */
@Component
public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    @Value("${app.crypto.secret-key:blockchain-certificate-system-2024}")
    private String secretKey;

    /**
     * 生成 AES 密钥
     */
    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    /**
     * 从字符串创建密钥
     */
    private SecretKey getKeyFromString(String key) {
        // 确保密钥长度为32字节（256位）
        byte[] keyBytes = new byte[32];
        byte[] sourceBytes = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(sourceBytes, 0, keyBytes, 0, Math.min(sourceBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * AES 加密
     * @param plainText 明文
     * @return 加密后的 Base64 字符串
     */
    public String encrypt(String plainText) {
        try {
            SecretKey key = getKeyFromString(secretKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES 解密
     * @param encryptedText 加密的 Base64 字符串
     * @return 解密后的明文
     */
    public String decrypt(String encryptedText) {
        try {
            SecretKey key = getKeyFromString(secretKey);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成随机密钥字符串
     */
    public static String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * 加密私钥用于安全存储
     * @param privateKey 私钥字符串
     * @return 加密后的私钥
     */
    public String encryptPrivateKey(String privateKey) {
        return encrypt(privateKey);
    }

    /**
     * 解密私钥用于使用
     * @param encryptedPrivateKey 加密的私钥
     * @return 解密后的私钥
     */
    public String decryptPrivateKey(String encryptedPrivateKey) {
        return decrypt(encryptedPrivateKey);
    }
}