package com.blockchain.certificate.infrastructure.ipfs;

/**
 * IPFS 操作异常类
 */
public class IpfsException extends Exception {

    public IpfsException(String message) {
        super(message);
    }

    public IpfsException(String message, Throwable cause) {
        super(message, cause);
    }

    public IpfsException(Throwable cause) {
        super(cause);
    }
}