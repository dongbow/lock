package com.github.lock.exception;

/**
 * @author wangdongbo
 * @since 2019/7/22.
 */
public class LockException extends RuntimeException {

    public LockException() {
        super();
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

}
