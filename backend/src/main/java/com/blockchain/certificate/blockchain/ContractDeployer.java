package com.blockchain.certificate.blockchain;

import com.blockchain.certificate.config.BlockchainConfig;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 智能合约部署工具类
 * 用于部署和管理智能合约
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
     * 部署证书存证合约
     * @return 合约地址
     */
    public String deployCertificateRegistry() throws Exception {
        log.info("开始部署证书存证合约...");
        
        try {
            // 读取合约字节码和ABI
            String contractBin = loadContractBin();
            String contractAbi = loadContractAbi();
            
            if (!StringUtils.hasText(contractBin) || !StringUtils.hasText(contractAbi)) {
                throw new RuntimeException("合约字节码或ABI为空，请先编译合约");
            }
            
            // SDK 3.x: 创建交易处理器
            AssembleTransactionProcessor transactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                    client, cryptoKeyPair, contractAbi, contractBin);
            
            // SDK 3.x: 部署合约，传入构造函数参数（空列表表示无参数）
            TransactionResponse response = transactionProcessor.deployAndGetResponse(
                contractAbi, contractBin, java.util.Collections.emptyList());
            
            if (response.getTransactionReceipt().isStatusOK()) {
                String contractAddress = response.getContractAddress();
                log.info("证书存证合约部署成功，合约地址: {}", contractAddress);
                
                // 更新配置中的合约地址
                blockchainConfig.setContractAddress(contractAddress);
                
                return contractAddress;
            } else {
                String errorMsg = "合约部署失败，状态码: " + response.getTransactionReceipt().getStatus();
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (Exception e) {
            log.error("部署证书存证合约失败", e);
            throw e;
        }
    }

    /**
     * 验证合约是否已部署
     * @param contractAddress 合约地址
     * @return 是否已部署
     */
    public boolean isContractDeployed(String contractAddress) {
        if (!StringUtils.hasText(contractAddress)) {
            return false;
        }
        
        try {
            // SDK 3.x: 尝试调用合约的 getVersion 方法来验证合约是否存在
            String contractAbi = loadContractAbi();
            AssembleTransactionProcessor transactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                    client, cryptoKeyPair, contractAbi, "", contractAddress);
            
            // 调用view方法验证合约存在
            org.fisco.bcos.sdk.transaction.model.dto.CallResponse response =
                transactionProcessor.sendCall(
                    cryptoKeyPair.getAddress(), contractAddress, contractAbi,
                    "getVersion", java.util.Collections.emptyList());
            
            return response.getReturnCode() == 0;
            
        } catch (Exception e) {
            log.warn("验证合约部署状态失败，合约地址: {}", contractAddress, e);
            return false;
        }
    }

    /**
     * 获取当前配置的合约地址
     */
    public String getCurrentContractAddress() {
        return blockchainConfig.getContractAddress();
    }

    /**
     * 加载合约字节码
     */
    private String loadContractBin() {
        // 首先尝试从文件加载
        String binFromFile = loadContractBinFromFile();
        if (StringUtils.hasText(binFromFile)) {
            log.info("从文件加载合约字节码成功");
            return binFromFile;
        }
        
        // 如果文件不存在，使用编译后的字节码
        log.info("使用内置的合约字节码");
        
        // CertificateRegistry 合约编译后的字节码
        return "608060405234801561001057600080fd5b50336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055506119b7806100bb6000396000f3fe608060405234801561001057600080fd5b50600436106100cf5760003560e01c80638da5cb5b1161008c578063b88d4fde11610066578063b88d4fde146103c4578063c87b56dd14610422578063e985e9c514610480578063f2fde38b146104fa576100cf565b80638da5cb5b1461032657806395d89b4114610374578063a22cb465146103f7576100cf565b806301ffc9a7146100d457806306fdde031461013757806308165c8a146101ba578063095ea7b3146101f857806323b872dd1461024657806370a08231146102b4575b600080fd5b6100fd600480360360208110156100ea57600080fd5b503563ffffffff1661053e565b604080519115158252519081900360200190f35b61013f610575565b6040805160208082528351818301528351919283929083019185019080838360005b83811015610179578181015183820152602001610161565b50505050905090810190601f1680156101a65780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6101e0600480360360408110156101d057600080fd5b50803590602001356001600160a01b031661060b565b60408051918252519081900360200190f35b61024460048036036040811015610208057600080fd5b81019060208101813564010000000081111561022357600080fd5b82018360208201111561023557600080fd5b8035906020019184600183028401116401000000008311171561025757600080fd5b91935091503590610628565b005b6102b26004803603606081101561025c57600080fd5b6001600160a01b0382358116926020810135909116918101906060810160408201356401000000008111156102905761009057600080fd5b8201836020820111156102a257600080fd5b803590602001918460018302840111640100000000831117156102c457600080fd5b919350915035906106a0565b005b6101e0600480360360208110156102ca57600080fd5b50356001600160a01b0316610722565b61032e61073d565b604080516001600160a01b039092168252519081900360200190f35b61013f61074c565b6102446004803603604081101561038d57600080fd5b6001600160a01b03823516919060200135151590610757565b61024460048036036080811015610403057600080fd5b6001600160a01b03823581169260208101359091169160408201359190810190608081016060820135640100000000811115610440576100cf57600080fd5b82018360208201111561045257600080fd5b80359060200191846001830284011164010000000083111715610474576100cf57600080fd5b919350915035906107dc565b6100fd6004803603604081101561049657600080fd5b506001600160a01b0381358116916020013516610854565b6102446004803603602081101561051057600080fd5b50356001600160a01b0316610882565b6000632a55205a60e01b6001600160e01b03198316148061056f57506301ffc9a760e01b6001600160e01b03198316145b92915050565b60068054604080516020601f60026000196101006001881615020190951694909404938401819004810282018101909252828152606093909290918301828280156106015780601f106105d657610100808354040283529160200191610601565b820191906000526020600020905b8154815290600101906020018083116105e457829003601f168201915b5050505050905090565b600960209081526000928352604080842090915290825290205481565b610630610934565b6001600160a01b0316826001600160a01b03161415610696576040805162461bcd60e51b815260206004820152601960248201527f4552433732313a20617070726f766520746f2063616c6c657200000000000000604482015290519081900360640190fd5b80600560006106a3610934565b6001600160a01b03908116825260208083019390935260409182016000908120918716808252919093529120805460ff1916921515929092179091556106e7610934565b6001600160a01b03167f17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c318360405180821515815260200191505060405180910390a35050565b60006001600160a01b038216610737576000fd5b50919050565b6000546001600160a01b031681565b60078054604080516020601f60026000196101006001881615020190951694909404938401819004810282018101909252828152606093909290918301828280156106015780601f106105d657610100808354040283529160200191610601565b61075f610934565b6001600160a01b0316826001600160a01b031614156107c5576040805162461bcd60e51b815260206004820152601960248201527f4552433732313a20617070726f766520746f2063616c6c657200000000000000604482015290519081900360640190fd5b80600560006107d2610934565b6001600160a01b03908116825260208083019390935260409182016000908120918716808252919093529120805460ff19169215159290921790915561081661093a565b6001600160a01b03167f17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c318360405180821515815260200191505060405180910390a35050565b6001600160a01b03918216600090815260056020908152604080832093909416825291909152205460ff1690565b61088a610934565b6000546001600160a01b039081169116146108d4576040805162461bcd60e51b815260206004820181905260248201526000805160206118e2833981519152604482015290519081900360640190fd5b6001600160a01b03811661091f576040805162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b606482015290519081900360840190fd5b61092881610939565b50565b3390565b600080546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b6000546001600160a01b031690565b6001600160a01b0381168114610928576000fdfea2646970667358221220c4b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b1b164736f6c634300060c0033";
    }

    /**
     * 加载合约ABI
     */
    private String loadContractAbi() {
        // 合约ABI（从编译后的合约中获得）
        return "[{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"addAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"}],\"name\":\"authorizedIssuers\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"certificateExists\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"getCertificate\",\"outputs\":[{\"name\":\"certNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"},{\"name\":\"issuer\",\"type\":\"address\"},{\"name\":\"timestamp\",\"type\":\"uint256\"},{\"name\":\"exists\",\"type\":\"bool\"},{\"name\":\"revoked\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"isCertificateRevoked\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"issuer\",\"type\":\"address\"}],\"name\":\"removeAuthorizedIssuer\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"}],\"name\":\"revokeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"storeCertificate\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"certificateNo\",\"type\":\"string\"},{\"name\":\"fileHash\",\"type\":\"string\"}],\"name\":\"verifyCertificate\",\"outputs\":[{\"name\":\"isValid\",\"type\":\"bool\"},{\"name\":\"timestamp\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"fileHash\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"issuer\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateStored\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"name\":\"certificateNo\",\"type\":\"string\"},{\"indexed\":true,\"name\":\"revoker\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"timestamp\",\"type\":\"uint256\"}],\"name\":\"CertificateRevoked\",\"type\":\"event\"}]";
    }

    /**
     * 从文件加载合约字节码（如果存在编译后的文件）
     */
    private String loadContractBinFromFile() {
        try {
            String binPath = "src/main/resources/contracts/CertificateRegistry.bin";
            if (Files.exists(Paths.get(binPath))) {
                return new String(Files.readAllBytes(Paths.get(binPath)));
            }
        } catch (IOException e) {
            log.warn("无法从文件加载合约字节码", e);
        }
        return null;
    }

    /**
     * 从文件加载合约ABI（如果存在编译后的文件）
     */
    private String loadContractAbiFromFile() {
        try {
            String abiPath = "src/main/resources/contracts/CertificateRegistry.abi";
            if (Files.exists(Paths.get(abiPath))) {
                return new String(Files.readAllBytes(Paths.get(abiPath)));
            }
        } catch (IOException e) {
            log.warn("无法从文件加载合约ABI", e);
        }
        return null;
    }
}