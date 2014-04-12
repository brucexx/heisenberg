/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongzhao@baidu.com
 */
public class NameableExecutor extends ThreadPoolExecutor {

    protected String name;

    public NameableExecutor(String name, int size, BlockingQueue<Runnable> queue, ThreadFactory factory) {
        super(size, size, Long.MAX_VALUE, TimeUnit.NANOSECONDS, queue, factory);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
