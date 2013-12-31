/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

/**
 * @author xiongzhao@baidu.com
 */
public abstract class NodeExecutor {
    /**
     * @return block until all tasks are finished
     */
    public abstract void terminate() throws InterruptedException;

}
