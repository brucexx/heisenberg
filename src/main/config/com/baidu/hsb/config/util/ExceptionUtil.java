/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.config.util;

import org.apache.log4j.Logger;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: ExceptionUtil.java, v 0.1 2014年3月17日 下午5:30:40 HI:brucest0078 Exp $
 */
public class ExceptionUtil {

    private static final Logger logger = Logger.getLogger("alarm");

    /**
     * 禁用构造函数
     */
    private ExceptionUtil() {
        // 禁用构造函数
    }

    /**
     * 捕捉错误日志并输出到日志文件：common-error.log
     * 
     * @param e
     *            异常堆栈
     * @param message
     *            错误日志上下文信息描述，尽量带上业务特征
     */
    public static void caught(Throwable e, Object... message) {
        logger.error(LoggerUtil.getLogString(message), e);
    }

}
