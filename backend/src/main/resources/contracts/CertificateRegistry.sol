pragma solidity ^0.4.25;

/**
 * 证书存证智能合约
 * 用于在区块链上存储和验证证书信息
 * 版本: 1.1.0 - 添加过期功能
 */
contract CertificateRegistry {
    
    // 证书记录结构
    struct CertificateRecord {
        string certificateNo;      // 证书编号
        string fileHash;          // 文件哈希值
        address issuer;           // 颁发者地址
        uint256 timestamp;        // 存证时间戳
        uint256 expiryDate;       // 过期时间戳 (0表示永不过期)
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
        uint256 timestamp,
        uint256 expiryDate
    );
    
    // 撤销事件
    event CertificateRevoked(
        string indexed certificateNo,
        address indexed revoker,
        uint256 timestamp
    );
    
    // 过期时间更新事件
    event CertificateExpiryUpdated(
        string indexed certificateNo,
        uint256 oldExpiryDate,
        uint256 newExpiryDate,
        address indexed updater
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
     * @param expiryDate 过期时间戳 (0表示永不过期)
     * @return 是否成功
     */
    function storeCertificate(
        string memory certificateNo,
        string memory fileHash,
        uint256 expiryDate
    ) public onlyAuthorizedIssuer returns (bool) {
        // 检查证书是否已存在
        require(!certificates[certificateNo].exists, "Certificate already exists");
        require(bytes(certificateNo).length > 0, "Certificate number cannot be empty");
        require(bytes(fileHash).length > 0, "File hash cannot be empty");
        // 如果设置了过期时间，必须大于当前时间
        require(expiryDate == 0 || expiryDate > block.timestamp, "Expiry date must be in the future");
        
        // 创建证书记录
        certificates[certificateNo] = CertificateRecord({
            certificateNo: certificateNo,
            fileHash: fileHash,
            issuer: msg.sender,
            timestamp: block.timestamp,
            expiryDate: expiryDate,
            exists: true,
            revoked: false
        });
        
        // 触发事件
        emit CertificateStored(certificateNo, fileHash, msg.sender, block.timestamp, expiryDate);
        
        return true;
    }
    
    /**
     * 存储证书信息（无过期时间，永久有效）
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return 是否成功
     */
    function storeCertificatePermanent(
        string memory certificateNo,
        string memory fileHash
    ) public onlyAuthorizedIssuer returns (bool) {
        return storeCertificate(certificateNo, fileHash, 0);
    }
    
    /**
     * 验证证书
     * @param certificateNo 证书编号
     * @param fileHash 文件哈希值
     * @return isValid 是否有效
     * @return timestamp 存证时间戳
     * @return status 状态码: 0=有效, 1=不存在, 2=已撤销, 3=已过期, 4=哈希不匹配
     */
    function verifyCertificate(
        string memory certificateNo,
        string memory fileHash
    ) public view returns (bool isValid, uint256 timestamp, uint8 status) {
        CertificateRecord memory record = certificates[certificateNo];
        
        // 检查证书是否存在
        if (!record.exists) {
            return (false, 0, 1); // 状态1: 不存在
        }
        
        // 检查证书是否已撤销
        if (record.revoked) {
            return (false, record.timestamp, 2); // 状态2: 已撤销
        }
        
        // 检查证书是否已过期
        if (record.expiryDate != 0 && block.timestamp > record.expiryDate) {
            return (false, record.timestamp, 3); // 状态3: 已过期
        }
        
        // 验证文件哈希
        bool hashMatches = keccak256(abi.encodePacked(record.fileHash)) ==
                          keccak256(abi.encodePacked(fileHash));
        
        if (!hashMatches) {
            return (false, record.timestamp, 4); // 状态4: 哈希不匹配
        }
        
        return (true, record.timestamp, 0); // 状态0: 有效
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
        uint256 expiryDate,
        bool exists,
        bool revoked,
        bool expired
    ) {
        CertificateRecord memory record = certificates[certificateNo];
        bool isExpired = record.expiryDate != 0 && block.timestamp > record.expiryDate;
        return (
            record.certificateNo,
            record.fileHash,
            record.issuer,
            record.timestamp,
            record.expiryDate,
            record.exists,
            record.revoked,
            isExpired
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
     * 检查证书是否已过期
     * @param certificateNo 证书编号
     * @return 是否已过期
     */
    function isCertificateExpired(string memory certificateNo) public view returns (bool) {
        CertificateRecord memory record = certificates[certificateNo];
        if (!record.exists) {
            return false;
        }
        if (record.expiryDate == 0) {
            return false; // 永不过期
        }
        return block.timestamp > record.expiryDate;
    }
    
    /**
     * 获取证书过期时间
     * @param certificateNo 证书编号
     * @return 过期时间戳 (0表示永不过期)
     */
    function getCertificateExpiryDate(string memory certificateNo) public view returns (uint256) {
        return certificates[certificateNo].expiryDate;
    }
    
    /**
     * 更新证书过期时间（仅限原颁发者或合约所有者）
     * @param certificateNo 证书编号
     * @param newExpiryDate 新的过期时间戳
     * @return 是否成功
     */
    function updateExpiryDate(
        string memory certificateNo,
        uint256 newExpiryDate
    ) public onlyAuthorizedIssuer returns (bool) {
        CertificateRecord storage record = certificates[certificateNo];
        
        require(record.exists, "Certificate does not exist");
        require(!record.revoked, "Certificate is revoked");
        require(record.issuer == msg.sender || msg.sender == owner,
                "Only issuer or owner can update expiry date");
        require(newExpiryDate == 0 || newExpiryDate > block.timestamp,
                "New expiry date must be in the future");
        
        uint256 oldExpiryDate = record.expiryDate;
        record.expiryDate = newExpiryDate;
        
        emit CertificateExpiryUpdated(certificateNo, oldExpiryDate, newExpiryDate, msg.sender);
        
        return true;
    }
    
    /**
     * 获取证书状态
     * @param certificateNo 证书编号
     * @return status 状态: 0=有效, 1=不存在, 2=已撤销, 3=已过期
     */
    function getCertificateStatus(string memory certificateNo) public view returns (uint8 status) {
        CertificateRecord memory record = certificates[certificateNo];
        
        if (!record.exists) {
            return 1; // 不存在
        }
        if (record.revoked) {
            return 2; // 已撤销
        }
        if (record.expiryDate != 0 && block.timestamp > record.expiryDate) {
            return 3; // 已过期
        }
        return 0; // 有效
    }
    
    /**
     * 获取合约版本
     */
    function getVersion() public pure returns (string memory) {
        return "1.1.0";
    }
}