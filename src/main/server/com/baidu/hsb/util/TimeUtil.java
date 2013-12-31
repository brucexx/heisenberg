/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.util;

/**
 * 弱精度的计时器，考虑性能不使用同步策略。
 * 
 * @author xiongzhao@baidu.com  
 */
public class TimeUtil {
    private static long CURRENT_TIME = System.currentTimeMillis();

    public static final long currentTimeMillis() {
        return CURRENT_TIME;
    }

    public static final void update() {
        CURRENT_TIME = System.currentTimeMillis();
    }

}
