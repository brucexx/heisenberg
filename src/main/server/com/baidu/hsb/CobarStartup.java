/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.helpers.LogLog;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: CobarStartup.java, v 0.1 2013年12月31日 下午1:40:32 HI:brucest0078 Exp $
 */
public final class CobarStartup {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    public static void main(String[] args) {
        try {
            System.setProperty("cobar.home", "D:/");
            // init
            CobarServer server = CobarServer.getInstance();
            server.beforeStart(dateFormat);

            // startup
            server.startup();
        } catch (Throwable e) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LogLog.error(sdf.format(new Date()) + " startup error", e);
            System.exit(-1);
        }
    }

}
