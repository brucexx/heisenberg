/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.config.util;

import org.apache.log4j.Logger;

import com.baidu.hsb.route.util.StringUtil;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: LoggerUtil.java, v 0.1 2014年3月17日 下午5:32:15 HI:brucest0078 Exp $
 */
public class LoggerUtil {

    /** 线程编号修饰符 */
    private static final char   THREAD_RIGHT_TAG     = ']';

    /** 线程编号修饰符 */
    private static final char   THREAD_LEFT_TAG      = '[';

    /** 换行符 */
    public static final char    ENTERSTR             = '\n';

    /** 逗号 */
    public static final char    COMMA                = ',';

    private static long         PERF_THRESHOLD       = 100;
    private static long         MULTI_PERF_THRESHOLD = 2000;

    private static final Logger perfLogger           = Logger.getLogger("sql-perf");
    private static final Logger digestLogger         = Logger.getLogger("sql-digest");

    /**
     * 禁用构造函数
     */
    private LoggerUtil() {
        // 禁用构造函数
    }

    static {
        if (StringUtil.isNotEmpty(System.getProperty("perf.threshold"))) {
            PERF_THRESHOLD = Long.parseLong(System.getProperty("perf.threshold"));
        }
    }

    public static void printDigest(Logger logger, long exeTime, String stmt) {
        if (exeTime >= MULTI_PERF_THRESHOLD) {
            perfLogger.info("[multi]" + exeTime + "ms,[" + stmt + "]");
        }
        digestLogger.info("[multi]" + exeTime + "ms,[" + stmt + "]");
    }

    public static void printDigest(Logger logger, long exeTime, long beginTime, String stmt) {
        if (exeTime >= PERF_THRESHOLD) {
            perfLogger.info("[single]" + exeTime + "ms," + beginTime + ",[" + stmt + "]");
        }
        digestLogger.info("[single]" + exeTime + "ms,[" + stmt + "]");
    }

    /**
     * 生成<font color="blue">调试</font>级别日志<br>
     * 可处理任意多个输入参数，并避免在日志级别不够时字符串拼接带来的资源浪费
     * 
     * @param logger
     * @param obj
     */
    public static void debug(Logger logger, Object... obj) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogString(obj));
        }
    }

    /**
     * 生成<font color="blue">通知</font>级别日志<br>
     * 可处理任意多个输入参数，并避免在日志级别不够时字符串拼接带来的资源浪费
     * 
     * @param logger
     * @param obj
     */
    public static void info(Logger logger, Object... obj) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogString(obj));
        }
    }

    /**
     * 生成<font color="brown">警告</font>级别日志<br>
     * 可处理任意多个输入参数，并避免在日志级别不够时字符串拼接带来的资源浪费
     * 
     * @param logger
     * @param obj
     */
    public static void warn(Logger logger, Object... obj) {
        logger.warn(getLogString(obj));
    }

    /**
     * 生成输出到日志的字符串
     * 
     * @param obj
     *            任意个要输出到日志的参数
     * @return
     */
    public static String getLogString(Object... obj) {
        StringBuilder log = new StringBuilder();
        log.append(THREAD_LEFT_TAG).append(Thread.currentThread().getId()).append(THREAD_RIGHT_TAG);
        for (Object o : obj) {
            log.append(o);
        }
        return log.toString();
    }

}
