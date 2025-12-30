package com.blockchain.certificate.infrastructure.blockchain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * WeBASE-Front HTTP API 客户端
 * 通过 WeBASE-Front 的 HTTP 接口与区块链交互，无需 SDK 证书
 * 
 * 支持合约版本: 1.1.0 (包含过期功能)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "webase.enabled", havingValue = "true", matchIfMissing = false)
public class WebaseFrontClient {

    @Value("${webase.front.url:http://localhost:5002}")
    private String webaseFrontUrl;

    @Value("${webase.front.group-id:1}")
    private Integer groupId;

    @Value("${webase.front.user-address:}")
    private String userAddress;

    @Value("${webase.contract.address:}")
    private String contractAddress;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 合约 ABI (v1.1.0 - 包含过期功能)
    // 注意：这个 ABI 需要在部署合约后从 WeBASE 获取最新版本
    private static final String CONTRACT_ABI = "["
        + "{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"addAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"authorizedIssuers\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"certificateExists\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"getCertificate\",\"outputs\":[{\"name\":\"certNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"},{\"name\":\"issuer\",\"type\":\"address\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"expiryDate\",\"type\":\"uint256\"},{\"name\":\"exists\",\"type\":\"bool\"},{\"name\":\"revoked\",\"type\":\"bool\"},{\"name\":\"expired\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"getCertificateExpiryDate\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"getCertificateStatus\",\"outputs\":[{\"name\":\"status\",\"type\":\"uint8\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[],\"name\":\"getVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"isCertificateExpired\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"isCertificateRevoked\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"removeAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},"
        + "{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"revokeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},"
        + "{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"},{\"name\":\"expiryDate\",\"type\":\"uint256\"}],\"name\":\"storeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},"
        + "{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"storeCertificatePermanent\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},"
        + "{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"newExpiryDate\",\"type\":\"uint256\"}],\"name\":\"updateExpiryDate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},"
        + "{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"verifyCertificate\",\"outputs\":[{\"name\":\"isValid\",\"type\":\"bool\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"status\",\"type\":\"uint8\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},"
        + "{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},"
        + "{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"fileHash\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"issuer\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"expiryDate\",\"type\":\"uint256\"}],\"name\":\"CertificateStored\",\"type\":\"event\"},"
        + "{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"revoker\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateRevoked\",\"type\":\"event\"},"
        + "{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"oldExpiryDate\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"newExpiryDate\",\"type\":\"uint256\"},{\"indexed\":true,\"name\":\"updater\",\"type\":\"address\"}],\"name\":\"CertificateExpiryUpdated\",\"type\":\"event\"}"
        + "]";

    public WebaseFrontClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        log.info("WeBASE-Front 客户端初始化");
        log.info("WeBASE-Front URL: {}", webaseFrontUrl);
        log.info("Group ID: {}", groupId);
        log.info("合约地址: {}", contractAddress);
    }

    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            String url = webaseFrontUrl + "/WeBASE-Front/" + groupId + "/web3/blockNumber";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("WeBASE-Front 连接测试成功，当前区块高度: {}", response.getBody());
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("WeBASE-Front 连接测试失败", e);
            return false;
        }
    }

    /**
     * 获取当前区块高度
     */
    public Long getBlockNumber() {
        try {
            String url = webaseFrontUrl + "/WeBASE-Front/" + groupId + "/web3/blockNumber";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return Long.parseLong(response.getBody());
        } catch (Exception e) {
            log.error("获取区块高度失败", e);
            return null;
        }
    }

    /**
     * 获取节点版本信息
     */
    public String getNodeVersion() {
        try {
            String url = webaseFrontUrl + "/WeBASE-Front/" + groupId + "/web3/clientVersion";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("获取节点版本失败", e);
            return null;
        }
    }

    /**
     * 存储证书到区块链（带过期时间）
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希
     * @param expiryDate 过期时间戳（秒），0表示永不过期
     */
    public TransactionResult storeCertificate(String certificateNo, String fileHash, long expiryDate) {
        log.info("通过 WeBASE-Front 存储证书，证书编号: {}, 文件哈希: {}, 过期时间: {}", 
                certificateNo, fileHash, expiryDate);

        try {
            // 构建请求
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "storeCertificate");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Arrays.asList(certificateNo, fileHash, expiryDate));

            // 发送交易
            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                
                TransactionResult txResult = new TransactionResult();
                txResult.setSuccess(true);
                txResult.setTransactionHash(result.path("transactionHash").asText());
                txResult.setBlockNumber(result.path("blockNumber").asLong());
                
                log.info("证书存储成功，交易哈希: {}", txResult.getTransactionHash());
                return txResult;
            } else {
                log.error("证书存储失败，响应: {}", response.getBody());
                TransactionResult txResult = new TransactionResult();
                txResult.setSuccess(false);
                txResult.setErrorMessage("HTTP 状态码: " + response.getStatusCode());
                return txResult;
            }
        } catch (Exception e) {
            log.error("证书存储失败", e);
            TransactionResult txResult = new TransactionResult();
            txResult.setSuccess(false);
            txResult.setErrorMessage(e.getMessage());
            return txResult;
        }
    }

    /**
     * 存储证书到区块链（永久有效）
     */
    public TransactionResult storeCertificatePermanent(String certificateNo, String fileHash) {
        return storeCertificate(certificateNo, fileHash, 0);
    }

    /**
     * 验证证书
     * @return VerificationResult 包含 isValid, timestamp, status
     *         status: 0=有效, 1=不存在, 2=已撤销, 3=已过期, 4=哈希不匹配
     */
    public VerificationResult verifyCertificate(String certificateNo, String fileHash) {
        log.info("通过 WeBASE-Front 验证证书，证书编号: {}, 文件哈希: {}", certificateNo, fileHash);

        try {
            // 构建请求（查询操作）
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "verifyCertificate");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Arrays.asList(certificateNo, fileHash));

            // 发送查询
            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                JsonNode output = result.path("output");
                
                VerificationResult verifyResult = new VerificationResult();
                if (output.isArray() && output.size() >= 3) {
                    verifyResult.setValid(output.get(0).asBoolean());
                    verifyResult.setTimestamp(output.get(1).asLong());
                    verifyResult.setStatus(output.get(2).asInt());
                }
                
                log.info("证书验证完成，有效性: {}, 状态码: {}", verifyResult.isValid(), verifyResult.getStatus());
                return verifyResult;
            } else {
                log.error("证书验证失败，响应: {}", response.getBody());
                return new VerificationResult();
            }
        } catch (Exception e) {
            log.error("证书验证失败", e);
            return new VerificationResult();
        }
    }

    /**
     * 获取证书信息（包含过期信息）
     */
    public CertificateInfo getCertificate(String certificateNo) {
        log.info("通过 WeBASE-Front 获取证书信息，证书编号: {}", certificateNo);

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "getCertificate");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Collections.singletonList(certificateNo));

            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                JsonNode output = result.path("output");
                
                CertificateInfo info = new CertificateInfo();
                // 新版合约返回8个字段: certNo, fileHash, issuer, timestamp, expiryDate, exists, revoked, expired
                if (output.isArray() && output.size() >= 8) {
                    info.setCertificateNo(output.get(0).asText());
                    info.setFileHash(output.get(1).asText());
                    info.setIssuer(output.get(2).asText());
                    info.setTimestamp(output.get(3).asLong());
                    info.setExpiryDate(output.get(4).asLong());
                    info.setExists(output.get(5).asBoolean());
                    info.setRevoked(output.get(6).asBoolean());
                    info.setExpired(output.get(7).asBoolean());
                }
                
                return info;
            }
        } catch (Exception e) {
            log.error("获取证书信息失败", e);
        }
        return null;
    }

    /**
     * 获取证书状态
     * @return 状态码: 0=有效, 1=不存在, 2=已撤销, 3=已过期
     */
    public int getCertificateStatus(String certificateNo) {
        log.info("通过 WeBASE-Front 获取证书状态，证书编号: {}", certificateNo);

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "getCertificateStatus");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Collections.singletonList(certificateNo));

            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                JsonNode output = result.path("output");
                
                if (output.isArray() && output.size() >= 1) {
                    return output.get(0).asInt();
                }
            }
        } catch (Exception e) {
            log.error("获取证书状态失败", e);
        }
        return -1; // 错误状态
    }

    /**
     * 撤销证书
     */
    public TransactionResult revokeCertificate(String certificateNo) {
        log.info("通过 WeBASE-Front 撤销证书，证书编号: {}", certificateNo);

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "revokeCertificate");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Collections.singletonList(certificateNo));

            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                
                TransactionResult txResult = new TransactionResult();
                txResult.setSuccess(true);
                txResult.setTransactionHash(result.path("transactionHash").asText());
                txResult.setBlockNumber(result.path("blockNumber").asLong());
                
                log.info("证书撤销成功，交易哈希: {}", txResult.getTransactionHash());
                return txResult;
            } else {
                TransactionResult txResult = new TransactionResult();
                txResult.setSuccess(false);
                txResult.setErrorMessage("HTTP 状态码: " + response.getStatusCode());
                return txResult;
            }
        } catch (Exception e) {
            log.error("证书撤销失败", e);
            TransactionResult txResult = new TransactionResult();
            txResult.setSuccess(false);
            txResult.setErrorMessage(e.getMessage());
            return txResult;
        }
    }

    /**
     * 更新证书过期时间
     * @param certificateNo 证书编号
     * @param newExpiryDate 新的过期时间戳（秒），0表示永不过期
     */
    public TransactionResult updateExpiryDate(String certificateNo, long newExpiryDate) {
        log.info("通过 WeBASE-Front 更新证书过期时间，证书编号: {}, 新过期时间: {}", certificateNo, newExpiryDate);

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "updateExpiryDate");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Arrays.asList(certificateNo, newExpiryDate));

            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                
                TransactionResult txResult = new TransactionResult();
                txResult.setSuccess(true);
                txResult.setTransactionHash(result.path("transactionHash").asText());
                txResult.setBlockNumber(result.path("blockNumber").asLong());
                
                log.info("证书过期时间更新成功，交易哈希: {}", txResult.getTransactionHash());
                return txResult;
            } else {
                TransactionResult txResult = new TransactionResult();
                txResult.setSuccess(false);
                txResult.setErrorMessage("HTTP 状态码: " + response.getStatusCode());
                return txResult;
            }
        } catch (Exception e) {
            log.error("证书过期时间更新失败", e);
            TransactionResult txResult = new TransactionResult();
            txResult.setSuccess(false);
            txResult.setErrorMessage(e.getMessage());
            return txResult;
        }
    }

    /**
     * 获取合约版本
     */
    public String getContractVersion() {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("groupId", groupId);
            request.put("user", userAddress);
            request.put("contractAddress", contractAddress);
            request.put("funcName", "getVersion");
            request.put("contractAbi", objectMapper.readTree(CONTRACT_ABI));
            request.put("funcParam", Collections.emptyList());

            String url = webaseFrontUrl + "/WeBASE-Front/trans/handle";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                JsonNode output = result.path("output");
                
                if (output.isArray() && output.size() >= 1) {
                    return output.get(0).asText();
                }
            }
        } catch (Exception e) {
            log.error("获取合约版本失败", e);
        }
        return null;
    }

    // ========== 内部类 ==========

    @Data
    public static class TransactionResult {
        private boolean success;
        private String transactionHash;
        private long blockNumber;
        private String errorMessage;
    }

    @Data
    public static class VerificationResult {
        private boolean valid;
        private long timestamp;
        private int status; // 0=有效, 1=不存在, 2=已撤销, 3=已过期, 4=哈希不匹配
        
        /**
         * 获取状态描述
         */
        public String getStatusDescription() {
            switch (status) {
                case 0: return "有效";
                case 1: return "不存在";
                case 2: return "已撤销";
                case 3: return "已过期";
                case 4: return "哈希不匹配";
                default: return "未知状态";
            }
        }
    }

    @Data
    public static class CertificateInfo {
        private String certificateNo;
        private String fileHash;
        private String issuer;
        private long timestamp;
        private long expiryDate;  // 过期时间戳，0表示永不过期
        private boolean exists;
        private boolean revoked;
        private boolean expired;  // 是否已过期
        
        /**
         * 判断证书是否永久有效
         */
        public boolean isPermanent() {
            return expiryDate == 0;
        }
    }

    // ========== Getter/Setter ==========

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getWebaseFrontUrl() {
        return webaseFrontUrl;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getUserAddress() {
        return userAddress;
    }
}