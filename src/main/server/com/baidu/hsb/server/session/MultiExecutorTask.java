/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.server.session;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: MutilQueryTask.java, v 0.1 2014年9月11日 下午12:13:16 HI:brucest0078 Exp $
 */
public class MultiExecutorTask {

    private static final ExecutorService pool = Executors.newFixedThreadPool(500);

    public static void runTask(Callable<Boolean> callable, int sec) throws InterruptedException,
                                                                   ExecutionException,
                                                                   TimeoutException {
        FutureTask<Boolean> task = new FutureTask<Boolean>(callable);
        pool.execute(task);
        task.get(sec, TimeUnit.SECONDS);
    }

}
