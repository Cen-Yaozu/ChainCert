package com.blockchain.certificate.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 证书编号生成器
 * 生成全局唯一的证书编号
 * 格式：CERT + 年月日时分秒 + 随机数 + 序列号
 * 例如：CERT20241211143025001234567890123
 */
@Component
public class CertificateNumberGenerator {

    private static final String PREFIX = "CERT";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    
    // 随机数位数
    private static final int RANDOM_LENGTH = 6;
    // 序列号位数
    private static final int SEQUENCE_LENGTH = 6;

    /**
     * 生成唯一证书编号
     * @return 证书编号
     */
    public String generateCertificateNumber() {
        // 时间戳部分（14位）：年月日时分秒
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        
        // 随机数部分（6位）
        String randomPart = generateRandomNumber(RANDOM_LENGTH);
        
        // 序列号部分（6位）：自增序列，循环使用
        String sequencePart = generateSequenceNumber();
        
        return PREFIX + timestamp + randomPart + sequencePart;
    }
    
    /**
     * 生成唯一证书编号（别名方法，用于兼容）
     * @return 证书编号
     */
    public String generate() {
        return generateCertificateNumber();
    }

    /**
     * 生成指定长度的随机数字符串
     * @param length 长度
     * @return 随机数字符串
     */
    private String generateRandomNumber(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成序列号
     * @return 格式化的序列号字符串
     */
    private String generateSequenceNumber() {
        long seq = SEQUENCE.getAndIncrement();
        // 如果序列号超过最大值，重置为1
        if (seq >= Math.pow(10, SEQUENCE_LENGTH)) {
            SEQUENCE.set(1);
            seq = 1;
        }
        return String.format("%0" + SEQUENCE_LENGTH + "d", seq);
    }

    /**
     * 验证证书编号格式是否正确
     * @param certificateNumber 证书编号
     * @return 是否有效
     */
    public boolean isValidCertificateNumber(String certificateNumber) {
        if (certificateNumber == null || certificateNumber.isEmpty()) {
            return false;
        }
        
        // 检查前缀
        if (!certificateNumber.startsWith(PREFIX)) {
            return false;
        }
        
        // 检查总长度：前缀(4) + 时间戳(14) + 随机数(6) + 序列号(6) = 30
        int expectedLength = PREFIX.length() + 14 + RANDOM_LENGTH + SEQUENCE_LENGTH;
        if (certificateNumber.length() != expectedLength) {
            return false;
        }
        
        // 检查时间戳部分是否为数字
        String timestampPart = certificateNumber.substring(PREFIX.length(), PREFIX.length() + 14);
        if (!timestampPart.matches("\\d{14}")) {
            return false;
        }
        
        // 检查随机数部分是否为数字
        String randomPart = certificateNumber.substring(PREFIX.length() + 14, PREFIX.length() + 14 + RANDOM_LENGTH);
        if (!randomPart.matches("\\d{" + RANDOM_LENGTH + "}")) {
            return false;
        }
        
        // 检查序列号部分是否为数字
        String sequencePart = certificateNumber.substring(PREFIX.length() + 14 + RANDOM_LENGTH);
        if (!sequencePart.matches("\\d{" + SEQUENCE_LENGTH + "}")) {
            return false;
        }
        
        return true;
    }

    /**
     * 从证书编号中提取时间戳
     * @param certificateNumber 证书编号
     * @return 时间戳字符串（yyyyMMddHHmmss）
     */
    public String extractTimestamp(String certificateNumber) {
        if (!isValidCertificateNumber(certificateNumber)) {
            throw new IllegalArgumentException("无效的证书编号格式");
        }
        return certificateNumber.substring(PREFIX.length(), PREFIX.length() + 14);
    }

    /**
     * 从证书编号中提取随机数部分
     * @param certificateNumber 证书编号
     * @return 随机数字符串
     */
    public String extractRandomPart(String certificateNumber) {
        if (!isValidCertificateNumber(certificateNumber)) {
            throw new IllegalArgumentException("无效的证书编号格式");
        }
        return certificateNumber.substring(PREFIX.length() + 14, PREFIX.length() + 14 + RANDOM_LENGTH);
    }

    /**
     * 从证书编号中提取序列号部分
     * @param certificateNumber 证书编号
     * @return 序列号字符串
     */
    public String extractSequencePart(String certificateNumber) {
        if (!isValidCertificateNumber(certificateNumber)) {
            throw new IllegalArgumentException("无效的证书编号格式");
        }
        return certificateNumber.substring(PREFIX.length() + 14 + RANDOM_LENGTH);
    }

    /**
     * 获取当前序列号值（用于测试）
     * @return 当前序列号
     */
    public long getCurrentSequence() {
        return SEQUENCE.get();
    }

    /**
     * 重置序列号（用于测试）
     */
    public void resetSequence() {
        SEQUENCE.set(1);
    }
}