/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author brucexx
 *
 */
public class ThreadPool {
    public static String COMMON = "common";
    private static Map<String, ExecutorService> pool = new HashMap<String, ExecutorService>();

    static {
        pool.put(COMMON, Executors.newFixedThreadPool(200));
    }

    public static ExecutorService getPool(String key) {
        return pool.get(key);
    }

    public static void execute(Runnable r) {
        getPool(COMMON).execute(r);
    }

}
