package com.blockchain.certificate.shared.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 数字签名工具类
 * 提供 RSA 数字签名生成和验证功能
 */
@Component
public class SignatureUtil {

    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;

    /**
     * 生成 RSA 密钥对
     * @return 密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成密钥对失败", e);
        }
    }

    /**
     * 将私钥转换为 Base64 字符串
     * @param privateKey 私钥
     * @return Base64 编码的私钥字符串
     */
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 将公钥转换为 Base64 字符串
     * @param publicKey 公钥
     * @return Base64 编码的公钥字符串
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 从 Base64 字符串恢复私钥
     * @param privateKeyString Base64 编码的私钥字符串
     * @return 私钥对象
     */
    public static PrivateKey stringToPrivateKey(String privateKeyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("恢复私钥失败", e);
        }
    }

    /**
     * 从 Base64 字符串恢复公钥
     * @param publicKeyString Base64 编码的公钥字符串
     * @return 公钥对象
     */
    public static PublicKey stringToPublicKey(String publicKeyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("恢复公钥失败", e);
        }
    }

    /**
     * 使用私钥对数据进行数��签名
     * @param data 要签名的数据
     * @param privateKey 私钥
     * @return Base64 编码的签名
     */
    public String sign(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("数字签名失败", e);
        }
    }

    /**
     * 使用私钥字符串对数据进行数字签名
     * @param data 要签名的数据
     * @param privateKeyString Base64 编码的私钥字符串
     * @return Base64 编码的签名
     */
    public String sign(String data, String privateKeyString) {
        PrivateKey privateKey = stringToPrivateKey(privateKeyString);
        return sign(data, privateKey);
    }

    /**
     * 使用公钥验证数字签名
     * @param data 原始数据
     * @param signatureString Base64 编码的签名
     * @param publicKey 公钥
     * @return 验证结果
     */
    public boolean verify(String data, String signatureString, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureString);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("签名验证失败", e);
        }
    }

    /**
     * 使用公钥字符串验证数字签名
     * @param data 原始数据
     * @param signatureString Base64 编码的签名
     * @param publicKeyString Base64 编码的公钥字符串
     * @return 验证结果
     */
    public boolean verify(String data, String signatureString, String publicKeyString) {
        PublicKey publicKey = stringToPublicKey(publicKeyString);
        return verify(data, signatureString, publicKey);
    }

    /**
     * 为审批数据生成签名
     * @param approvalData 审批数据（JSON 格式）
     * @param privateKeyString 审批人的私钥
     * @return 数字签名
     */
    public String signApprovalData(String approvalData, String privateKeyString) {
        return sign(approvalData, privateKeyString);
    }

    /**
     * 验证审批数据的签名
     * @param approvalData 审批数据（JSON 格式）
     * @param signatureString 数字签名
     * @param publicKeyString 审批人的公钥
     * @return 验证结果
     */
    public boolean verifyApprovalSignature(String approvalData, String signatureString, String publicKeyString) {
        return verify(approvalData, signatureString, publicKeyString);
    }

    /**
     * 生成用户密钥对并返回字符串格式
     * @return 包含私钥和公钥字符串的数组 [privateKey, publicKey]
     */
    public static String[] generateKeyPairStrings() {
        KeyPair keyPair = generateKeyPair();
        String privateKeyString = privateKeyToString(keyPair.getPrivate());
        String publicKeyString = publicKeyToString(keyPair.getPublic());
        return new String[]{privateKeyString, publicKeyString};
    }
}