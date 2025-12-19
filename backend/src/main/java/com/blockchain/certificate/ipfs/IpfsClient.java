package com.blockchain.certificate.ipfs;

/**
 * IPFS 客户端接口
 * 定义 IPFS 文件操作的基本方法
 * 注意：当前使用 MockIpfsClient 实现，不需要真实的 IPFS 连接
 */
public interface IpfsClient {

    /**
     * 上传文件到 IPFS
     * 
     * @param fileName 文件名
     * @param content 文件内容
     * @return IPFS CID
     * @throws IpfsException IPFS 操作异常
     */
    String uploadFile(String fileName, byte[] content) throws IpfsException;

    /**
     * 从 IPFS 下载文件
     * 
     * @param cid IPFS CID
     * @return 文件内容
     * @throws IpfsException IPFS 操作异常
     */
    byte[] downloadFile(String cid) throws IpfsException;

    /**
     * 检查文件是否存在于 IPFS
     * 
     * @param cid IPFS CID
     * @return 文件是否存在
     */
    boolean fileExists(String cid);

    /**
     * 删除 IPFS 文件（实际上是取消固定）
     * 
     * @param cid IPFS CID
     * @throws IpfsException IPFS 操作异常
     */
    void deleteFile(String cid) throws IpfsException;

    /**
     * 获取 IPFS 节点信息
     * 
     * @return 节点信息
     * @throws IpfsException IPFS 操作异常
     */
    String getNodeInfo() throws IpfsException;

    /**
     * 测试 IPFS 连接
     * 
     * @return 连接是否正常
     */
    boolean testConnection();

    /**
     * 获取 IPFS 网络统计信息
     * 
     * @return 网络统计信息
     * @throws IpfsException IPFS 操作异常
     */
    String getNetworkStats() throws IpfsException;
}