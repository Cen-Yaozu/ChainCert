pragma solidity ^0.4.25;

/**
 * 证书存证智能合约
 * 用于在区块链上存储和验证证书信息
 */
contract CertificateRegistry {
    
    // 证书记录结构
    struct CertificateRecord {
        string certificateNo;      // 证书编号
        string fileHash;          // 文件哈希值
        address issuer;           // 颁发者地址
        uint256 timestamp;        // 存证时间戳
        bool exists;              // 是否存在
        bool revoked;             // 是否已撤销
    }
    
    // 证书编号到证书记录的映射
    mapping(string => CertificateRecord) private certificates;
    
    // 存证事件
    event CertificateStored(
        string indexed certificateNo,
        string fileHash,
        address indexed issuer,
        uint256 timestamp
    );
    
    // 撤销事件
    event CertificateRevoked(
        string indexed certificateNo,
        address indexed revoker,
        uint256 timestamp
    );
    
    // 合约所有者
    address public owner;
    
    // 授权颁发者映射
    mapping(address => bool) public authorizedIssuers;
    
    /**
     * 构造函数
     */
    constructor() public {
        owner = msg.sender;
        authorizedIssuers[msg.sender] = true;
    }
    
    /**
     * 修饰符：仅所有者
     */
    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this function");
        _;
    }
    
    /**
     * 修饰符：仅授权颁发者
     */
    modifier onlyAuthorizedIssuer() {
        require(authorizedIssuers[msg.sender], "Only authorized issuer can call this function");
        _;
    }
    
    /**
     * 添加授权颁发者
     */
    function addAuthorizedIssuer(address issuer) public onlyOwner {
        authorizedIssuers[issuer] = true;
    }
    
    /**
     * 移除授权颁发者
     */
    function removeAuthorizedIssuer(address issuer) public onlyOwner {
        authorizedIssuers[issuer] = false;
    }
    
    /**
     * 存储证书信息
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 是否成功
     */
    function storeCertificate(
        string memory certificateNo,
        string memory fileHash
    ) public onlyAuthorizedIssuer returns (bool) {
        // 检查证书是否已存在
        require(!certificates[certificateNo].exists, "Certificate already exists");
        require(bytes(certificateNo).length > 0, "Certificate number cannot be empty");
        require(bytes(fileHash).length > 0, "File hash cannot be empty");
        
        // 创建证书记录
        certificates[certificateNo] = CertificateRecord({
            certificateNo: certificateNo,
            fileHash: fileHash,
            issuer: msg.sender,
            timestamp: block.timestamp,
            exists: true,
            revoked: false
        });
        
        // 触发事件
        emit CertificateStored(certificateNo, fileHash, msg.sender, block.timestamp);
        
        return true;
    }
    
    /**
     * 验证证书
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 是否有效和存证时间戳
     */
    function verifyCertificate(
        string memory certificateNo,
        string memory fileHash
    ) public view returns (bool isValid, uint256 timestamp) {
        CertificateRecord memory record = certificates[certificateNo];
        
        // 检查证书是否存在
        if (!record.exists) {
            return (false, 0);
        }
        
        // 检查证书是否已撤销
        if (record.revoked) {
            return (false, 0);
        }
        
        // 验证文件哈希
        bool hashMatches = keccak256(abi.encodePacked(record.fileHash)) == 
                          keccak256(abi.encodePacked(fileHash));
        
        return (hashMatches, record.timestamp);
    }
    
    /**
     * 获取证书信息
     * @param certificateNo 证书编号
     * @return 证书详细信息
     */
    function getCertificate(
        string memory certificateNo
    ) public view returns (
        string memory certNo,
        string memory fileHash,
        address issuer,
        uint256 timestamp,
        bool exists,
        bool revoked
    ) {
        CertificateRecord memory record = certificates[certificateNo];
        return (
            record.certificateNo,
            record.fileHash,
            record.issuer,
            record.timestamp,
            record.exists,
            record.revoked
        );
    }
    
    /**
     * 撤销证书
     * @param certificateNo 证书编号
     * @return 是否成功
     */
    function revokeCertificate(
        string memory certificateNo
    ) public onlyAuthorizedIssuer returns (bool) {
        CertificateRecord storage record = certificates[certificateNo];
        
        // 检查证书是否存在
        require(record.exists, "Certificate does not exist");
        require(!record.revoked, "Certificate already revoked");
        
        // 只有原颁发者或合约所有者可以撤销
        require(record.issuer == msg.sender || msg.sender == owner, 
                "Only issuer or owner can revoke certificate");
        
        // 标记为已撤销
        record.revoked = true;
        
        // 触发事件
        emit CertificateRevoked(certificateNo, msg.sender, block.timestamp);
        
        return true;
    }
    
    /**
     * 检查证书是否存在
     * @param certificateNo 证书编号
     * @return 是否存在
     */
    function certificateExists(string memory certificateNo) public view returns (bool) {
        return certificates[certificateNo].exists;
    }
    
    /**
     * 检查证书是否已撤销
     * @param certificateNo 证书编号
     * @return 是否已撤销
     */
    function isCertificateRevoked(string memory certificateNo) public view returns (bool) {
        return certificates[certificateNo].revoked;
    }
    
    /**
     * 获取合约版本
     */
    function getVersion() public pure returns (string memory) {
        return "1.0.0";
    }
}