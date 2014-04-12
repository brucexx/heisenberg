/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.helpers.LogLog;

import com.baidu.hsb.route.util.StringUtil;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: CobarStartup.java, v 0.1 2013年12月31日 下午1:40:32 HI:brucest0078 Exp $
 */
public final class HeisenbergStartup {
    private static final String              dateFormat  = "yyyy-MM-dd HH:mm:ss";

    private static final Map<String, String> map         = new HashMap<String, String>();

    private static final String              CONFIG_PATH = "-conf";

    public static boolean hasSelfConfigPath() {
        return StringUtil.isNotEmpty(map.get(CONFIG_PATH));
    }

    public static String getConfigPath() {
        return map.get(CONFIG_PATH);
    }

    public static void main(String[] args) {

        if (args.length > 0) {
            for (int i = 0; i < args.length; i = i + 2) {
                map.put(args[i], args[i + 1]);
            }
        }

        long s = System.currentTimeMillis();
        try {
            if (StringUtil.isEmpty(System.getProperty("hsb.home"))) {
                System.setProperty("hsb.home", "D:/");
            }
            if (StringUtil.isEmpty(System.getProperty("hsb.log.home"))) {
                System.setProperty("hsb.log.home", System.getProperty("hsb.home"));
            }
            String fp = null;

            if (hasSelfConfigPath()) {
                fp = new File(getConfigPath()).getPath() + File.separator + "hsb.properties";
            } else {
                URL uri = HeisenbergStartup.class.getResource("/hsb.properties");
                fp = uri.getPath();
            }

            HeisenbergContext.load(fp);

            // init
            HeisenbergServer server = HeisenbergServer.getInstance();
            server.beforeStart(dateFormat);

            // startup
            server.startup();
        } catch (Throwable e) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LogLog.error(sdf.format(new Date()) + " startup error", e);
            System.exit(-1);
        } finally {
            System.out.println("start hsb time:" + (System.currentTimeMillis() - s) + "ms");
        }
    }

}
