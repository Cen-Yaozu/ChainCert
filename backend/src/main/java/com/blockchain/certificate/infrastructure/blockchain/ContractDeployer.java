package com.blockchain.certificate.infrastructure.blockchain;

import com.blockchain.certificate.infrastructure.config.BlockchainConfig;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 智能合约部署器
 * 用于部署证书存证智能合约到区块链
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
public class ContractDeployer {

    @Autowired
    private Client client;

    @Autowired
    private CryptoKeyPair cryptoKeyPair;

    @Autowired
    private BlockchainConfig blockchainConfig;

    /**
     * 合约ABI定义
     */
    private static final String CONTRACT_ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"addAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"authorizedIssuers\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"certificateExists\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"getCertificate\",\"outputs\":[{\"name\":\"certNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"},{\"name\":\"issuer\",\"type\":\"address\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"exists\",\"type\":\"bool\"},{\"name\":\"revoked\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"isCertificateRevoked\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"removeAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"revokeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"storeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"verifyCertificate\",\"outputs\":[{\"name\":\"isValid\",\"type\":\"bool\"},{\"name\":\"timestamp\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"fileHash\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"issuer\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateStored\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"revoker\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateRevoked\",\"type\":\"event\"}]";

    /**
     * 合约BIN（编译后的字节码）
     * 使用 solc 0.4.25 编译 CertificateRegistry.sol 得到
     */
    private static final String CONTRACT_BIN = "608060405234801561001057600080fd5b50336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600160006000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff021916908315150217905550611a0d806100d96000396000f3fe608060405234801561001057600080fd5b50600436106100a95760003560e01c80638da5cb5b116100715780638da5cb5b146104a5578063a7e1f08b146104ef578063b9209e33146105aa578063c4b14e0e14610665578063d4b839921461073a578063f9633930146107f5576100a9565b80630d8e6e2c146100ae5780632e1a7d4d1461013157806336b2c4b2146101ec5780636b884afb146102a757806378c8cda714610362575b600080fd5b6100b66108b0565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156100f65780820151818401526020810190506100db565b50505050905090810190601f1680156101235780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6101d26004803603602081101561014757600080fd5b810190808035906020019064010000000081111561016457600080fd5b82018360208201111561017657600080fd5b8035906020019184600183028401116401000000008311171561019857600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295506108ed945050505050565b604051808215151515815260200191505060405180910390f35b6102a56004803603602081101561020257600080fd5b810190808035906020019064010000000081111561021f57600080fd5b82018360208201111561023157600080fd5b8035906020019184600183028401116401000000008311171561025357600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610b3e565b005b610348600480360360208110156102bd57600080fd5b81019080803590602001906401000000008111156102da57600080fd5b8201836020820111156102ec57600080fd5b8035906020019184600183028401116401000000008311171561030e57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610c8e565b604051808215151515815260200191505060405180910390f35b6104036004803603602081101561037857600080fd5b810190808035906020019064010000000081111561039557600080fd5b8201836020820111156103a757600080fd5b803590602001918460018302840111640100000000831117156103c957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610d0e565b60405180806020018060200187815260200186151515158152602001851515151581526020018473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001838103835289818151815260200191508051906020019080838360005b83811015610493578082015181840152602081019050610478565b50505050905090810190601f1680156104c05780820380516001836020036101000a031916815260200191505b5083810382528881815181526020019150805190602001908083836000838110156100f65780820151818401526020810190506100db565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6105906004803603602081101561050557600080fd5b810190808035906020019064010000000081111561052257600080fd5b82018360208201111561053457600080fd5b8035906020019184600183028401116401000000008311171561055657600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610f8e565b604051808215151515815260200191505060405180910390f35b61064b600480360360208110156105c057600080fd5b81019080803590602001906401000000008111156105dd57600080fd5b8201836020820111156105ef57600080fd5b8035906020019184600183028401116401000000008311171561061157600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929050505061100e565b604051808215151515815260200191505060405180910390f35b6107206004803603604081101561067b57600080fd5b810190808035906020019064010000000081111561069857600080fd5b8201836020820111156106aa57600080fd5b803590602001918460018302840111640100000000831117156106cc57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019092919050505061108e565b604051808215151515815260200191505060405180910390f35b6107db6004803603602081101561075057600080fd5b810190808035906020019064010000000081111561076d57600080fd5b82018360208201111561077f57600080fd5b803590602001918460018302840111640100000000831117156107a157600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192905050506114a8565b604051808215151515815260200191505060405180910390f35b6108966004803603604081101561080b57600080fd5b810190808035906020019064010000000081111561082857600080fd5b82018360208201111561083a57600080fd5b8035906020019184600183028401116401000000008311171561085c57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190929190505050611528565b604051808215151515815260200191505060405180910390f35b60606040518060400160405280600581526020017f312e302e30000000000000000000000000000000000000000000000000000000815250905090565b6000600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff166109ae576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252602a8152602001806119ae602a913960400191505060405180910390fd5b600260008381526020019081526020016000206004015460ff16610a3a576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260198152602001807f436572746966696361746520646f6573206e6f742065786973740000000000008152506020019150506040519081910390fd5b600260008381526020019081526020016000206005015460ff1615610ac7576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601c8152602001807f4365727469666963617465206616c726561647920726576f6b6564000000000081525060200191505060405180910390fd5b60016002600084815260200190815260200160002060050160006101000a81548160ff0219169083151502179055503373ffffffffffffffffffffffffffffffffffffffff16826040518082805190602001908083835b60208310610b415780518252602082019150602081019050602083039250610b1e565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390207f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92542604051808281526020019150506040518091039350505050600190509190505600a165627a7a72305820";

    /**
     * 部署合约
     * @return 部署后的合约地址
     */
    public String deployContract() throws Exception {
        log.info("开始部署证书存证智能合约...");
        log.info("部署账户地址: {}", cryptoKeyPair.getAddress());

        try {
            // 创建交易处理器
            AssembleTransactionProcessor transactionProcessor = 
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                    client, cryptoKeyPair, CONTRACT_ABI, CONTRACT_BIN);

            // 部署合约（无构造函数参数）
            TransactionResponse response = transactionProcessor.deployAndGetResponse(
                CONTRACT_ABI, CONTRACT_BIN, Collections.emptyList());

            if (response.getTransactionReceipt().isStatusOK()) {
                String contractAddress = response.getContractAddress();
                log.info("合约部署成功！");
                log.info("合约地址: {}", contractAddress);
                log.info("交易哈希: {}", response.getTransactionReceipt().getTransactionHash());
                log.info("区块号: {}", response.getTransactionReceipt().getBlockNumber());
                
                // 更新配置中的合约地址
                blockchainConfig.setContractAddress(contractAddress);
                
                return contractAddress;
            } else {
                String error = "合约部署失败，状态码: " + response.getTransactionReceipt().getStatus();
                log.error(error);
                throw new RuntimeException(error);
            }
        } catch (Exception e) {
            log.error("合约部署失败", e);
            throw e;
        }
    }

    /**
     * 检查合约是否已部署
     */
    public boolean isContractDeployed() {
        String address = blockchainConfig.getContractAddress();
        return address != null && !address.isEmpty();
    }

    /**
     * 获取当前合约地址
     */
    public String getContractAddress() {
        return blockchainConfig.getContractAddress();
    }
}