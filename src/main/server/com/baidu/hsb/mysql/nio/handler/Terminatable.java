/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio.handler;

/**
 * @author xiongzhao@baidu.com
 */
public interface Terminatable {
    void terminate(Runnable runnable);
}
