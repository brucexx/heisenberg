/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.exception;

/**
 * @author xiongzhao@baidu.com
 */
public class UnknownDataNodeException extends RuntimeException {
    private static final long serialVersionUID = -3752985849571697432L;

    public UnknownDataNodeException() {
        super();
    }

    public UnknownDataNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownDataNodeException(String message) {
        super(message);
    }

    public UnknownDataNodeException(Throwable cause) {
        super(cause);
    }

}
