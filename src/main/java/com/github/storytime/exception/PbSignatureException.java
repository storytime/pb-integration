package com.github.storytime.exception;

/**
 * Sometime in case of RIGHT signature, bank return "invalid signature" response.
 * To fix this issue roll back for one day in sync procedure is needed
 */
public class PbSignatureException extends RuntimeException {
    public PbSignatureException(String message) {
        super(message);
    }
}
