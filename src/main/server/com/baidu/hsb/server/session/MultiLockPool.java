/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.server.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import com.baidu.hsb.route.util.StringUtil;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: MultiLock.java, v 0.1 2014年9月11日 下午3:07:44 HI:brucest0078 Exp $
 */
public class MultiLockPool {

    private static final Map<String, ReentrantLock> lockPool = new HashMap<String, ReentrantLock>();

    private static final Object                     lock     = new Object();

    public static void lock(String id) {
        if (StringUtil.isEmpty(id)) {
            return;
        }

        ReentrantLock lock = null;
        if ((lock = lockPool.get(id)) == null) {
            synchronized (MultiLockPool.lock) {
                if ((lock = lockPool.get(id)) == null) {
                    lockPool.put(id, new ReentrantLock());
                    lock = lockPool.get(id);
                }
            }
        }
        lock.lock();
    }

    public static void releaseLock(String id) {
        if (StringUtil.isEmpty(id)) {
            return;
        }

        ReentrantLock lock = null;
        if ((lock = lockPool.get(id)) == null) {
            synchronized (MultiLockPool.lock) {
                if ((lock = lockPool.get(id)) == null) {
                    lockPool.put(id, new ReentrantLock());
                    lock = lockPool.get(id);
                }
            }
        }
        lock.unlock();
    }

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            final int k = i;

            try {
                pool.execute(new Runnable() {

                    @Override
                    public void run() {
                        MultiLockPool.lock("11");
                        try {
                            Thread.sleep(1000);
                            System.out.println("-->" + k);
                        } catch (InterruptedException e) {

                        } finally {
                            MultiLockPool.releaseLock("11");
                        }

                    }
                });
            } finally {

            }
        }

    }

}
