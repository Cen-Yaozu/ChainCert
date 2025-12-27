package com.blockchain.certificate.infrastructure.ipfs;

import com.blockchain.certificate.infrastructure.config.IpfsConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 真实的 IPFS 客户端实现
 * 通过 HTTP API 与 IPFS 节点通信
 * 
 * 启用条件：配置 ipfs.enabled=true
 */
@Component
@ConditionalOnProperty(name = "ipfs.enabled", havingValue = "true")
@Slf4j
public class RealIpfsClient implements IpfsClient {

    private final IpfsConfig ipfsConfig;
    private final ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;
    private String baseUrl;

    public RealIpfsClient(IpfsConfig ipfsConfig) {
        this.ipfsConfig = ipfsConfig;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        this.baseUrl = String.format("http://%s:%d/api/v0", 
                ipfsConfig.getHost(), ipfsConfig.getPort());
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(ipfsConfig.getTimeout())
                .setSocketTimeout(ipfsConfig.getTimeout())
                .setConnectionRequestTimeout(ipfsConfig.getTimeout())
                .build();
        
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        
        log.info("IPFS 客户端初始化完成，连接地址: {}", baseUrl);
    }

    @PreDestroy
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
                log.info("IPFS 客户端已关闭");
            } catch (IOException e) {
                log.error("关闭 IPFS 客户端失败", e);
            }
        }
    }

    /**
     * 上传文件到 IPFS
     * 
     * @param fileName 文件名
     * @param content 文件内容
     * @return IPFS CID
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public String uploadFile(String fileName, byte[] content) throws IpfsException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IpfsException("文件名不能为空");
        }
        
        if (content == null || content.length == 0) {
            throw new IpfsException("文件内容不能为空");
        }

        log.debug("上传文件到 IPFS: {}, 大小: {} bytes", fileName, content.length);

        HttpPost httpPost = new HttpPost(baseUrl + "/add");
        
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", content, ContentType.APPLICATION_OCTET_STREAM, fileName)
                .build();
        
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (statusCode != 200) {
                log.error("IPFS 上传失败，状态码: {}, 响应: {}", statusCode, responseBody);
                throw new IpfsException("IPFS 上传失败，状态码: " + statusCode);
            }
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String hash = jsonNode.get("Hash").asText();
            
            log.info("文件上传成功: {} -> CID: {}", fileName, hash);
            return hash;
            
        } catch (IOException e) {
            log.error("上传文件到 IPFS 失败: {}", fileName, e);
            throw new IpfsException("上传文件到 IPFS 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 IPFS 下载文件
     * 
     * @param cid IPFS CID
     * @return 文件内容
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public byte[] downloadFile(String cid) throws IpfsException {
        if (cid == null || cid.trim().isEmpty()) {
            throw new IpfsException("CID 不能为空");
        }

        log.debug("从 IPFS 下载文件: {}", cid);

        HttpPost httpPost = new HttpPost(baseUrl + "/cat?arg=" + cid);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.error("IPFS 下载失败，状态码: {}, 响应: {}", statusCode, errorBody);
                throw new IpfsException("IPFS 下载失败，状态码: " + statusCode);
            }
            
            byte[] content = EntityUtils.toByteArray(response.getEntity());
            log.debug("文件下载成功: {}, 大小: {} bytes", cid, content.length);
            return content;
            
        } catch (IOException e) {
            log.error("从 IPFS 下载文件失败: {}", cid, e);
            throw new IpfsException("从 IPFS 下载文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在于 IPFS
     * 通过尝试获取文件状态来判断
     * 
     * @param cid IPFS CID
     * @return 文件是否存在
     */
    @Override
    public boolean fileExists(String cid) {
        if (cid == null || cid.trim().isEmpty()) {
            return false;
        }

        log.debug("检查 IPFS 文件是否存在: {}", cid);

        // 使用 object/stat 命令检查文件是否存在
        HttpPost httpPost = new HttpPost(baseUrl + "/object/stat?arg=" + cid);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            boolean exists = response.getStatusLine().getStatusCode() == 200;
            log.debug("IPFS 文件存在检查: {} -> {}", cid, exists);
            return exists;
        } catch (IOException e) {
            log.warn("检查 IPFS 文件存在性失败: {}", cid, e);
            return false;
        }
    }

    /**
     * 删除 IPFS 文件（取消固定）
     * 注意：IPFS 中的文件不能真正删除，只能取消固定
     * 
     * @param cid IPFS CID
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public void deleteFile(String cid) throws IpfsException {
        if (cid == null || cid.trim().isEmpty()) {
            throw new IpfsException("CID 不能为空");
        }

        log.debug("取消固定 IPFS 文件: {}", cid);

        HttpPost httpPost = new HttpPost(baseUrl + "/pin/rm?arg=" + cid);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode != 200) {
                String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                // 如果文件本来就没有被固定，不算错误
                if (!errorBody.contains("not pinned")) {
                    log.warn("取消固定 IPFS 文件失败，状态码: {}, 响应: {}", statusCode, errorBody);
                }
            } else {
                log.info("IPFS 文件取消固定成功: {}", cid);
            }
        } catch (IOException e) {
            log.error("取消固定 IPFS 文件失败: {}", cid, e);
            throw new IpfsException("取消固定 IPFS 文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 IPFS 节点信息
     * 
     * @return 节点信息 JSON 字符串
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public String getNodeInfo() throws IpfsException {
        log.debug("获取 IPFS 节点信息");

        HttpPost httpPost = new HttpPost(baseUrl + "/id");

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (statusCode != 200) {
                log.error("获取 IPFS 节点信息失败，状态码: {}", statusCode);
                throw new IpfsException("获取 IPFS 节点信息失败，状态码: " + statusCode);
            }
            
            return responseBody;
        } catch (IOException e) {
            log.error("获取 IPFS 节点信息失败", e);
            throw new IpfsException("获取 IPFS 节点信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 测试 IPFS 连接
     * 
     * @return 连接是否正常
     */
    @Override
    public boolean testConnection() {
        log.debug("测试 IPFS 连接: {}", baseUrl);

        HttpPost httpPost = new HttpPost(baseUrl + "/id");

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            boolean connected = response.getStatusLine().getStatusCode() == 200;
            log.info("IPFS 连接测试: {}", connected ? "成功" : "失败");
            return connected;
        } catch (IOException e) {
            log.warn("IPFS 连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 IPFS 网络统计信息
     * 
     * @return 网络统计信息 JSON 字符串
     * @throws IpfsException IPFS 操作异常
     */
    @Override
    public String getNetworkStats() throws IpfsException {
        log.debug("获取 IPFS 网络统计信息");

        HttpPost httpPost = new HttpPost(baseUrl + "/stats/bw");

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (statusCode != 200) {
                log.error("获取 IPFS 网络统计信息失败，状态码: {}", statusCode);
                throw new IpfsException("获取 IPFS 网络统计信息失败，状态码: " + statusCode);
            }
            
            return responseBody;
        } catch (IOException e) {
            log.error("获取 IPFS 网络统计信息失败", e);
            throw new IpfsException("获取 IPFS 网络统计信息失败: " + e.getMessage(), e);
        }
    }
}