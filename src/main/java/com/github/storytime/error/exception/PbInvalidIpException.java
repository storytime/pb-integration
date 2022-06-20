package com.github.storytime.error.exception;

/**
 * happens when we are connecting to PB with not allowed IP
 */
public class PbInvalidIpException extends RuntimeException {
    public PbInvalidIpException(String message) {
        super(message);
    }
}
