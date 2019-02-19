/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.helpers.LogLog;

import com.baidu.hsb.route.util.StringUtil;

import sun.management.VMManagement;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: CobarStartup.java, v 0.1 2013年12月31日 下午1:40:32 HI:brucest0078 Exp $
 */
public final class HeisenbergStartup {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private static final Map<String, String> map = new HashMap<String, String>();

    private static final String CONFIG_PATH = "-c";

    private static String PID = "";

    static {
        if (StringUtil.isEmpty(PID)) {
            PID = "" + jvmPid();
        }
    }

    public static boolean hasSelfConfigPath() {
        return StringUtil.isNotEmpty(map.get(CONFIG_PATH));
    }

    public static String getConfigPath() {
        return map.get(CONFIG_PATH);
    }

    public static final int jvmPid() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            VMManagement mgmt = (VMManagement) jvm.get(runtime);
            Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
            pidMethod.setAccessible(true);
            int pid = (Integer) pidMethod.invoke(mgmt);
            return pid;
        } catch (Exception e) {
            return -1;
        }
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
                System.setProperty("hsb.home", "/Users/brucexx/tmp/hsb");
            }
            if (StringUtil.isEmpty(System.getProperty("hsb.log.home"))) {
                System.setProperty("hsb.log.home", System.getProperty("hsb.home") + "/logs");
            }
            File file = new File(System.getProperty("hsb.log.home"));
            if (!file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }
            file = new File(System.getProperty("hsb.home"));
            if (!file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }

            System.out.println("hsb.home-->" + System.getProperty("hsb.home"));
            System.out.println("hsb.log.home-->" + System.getProperty("hsb.log.home"));

            String fp = null;
            String pidFile = null;
            String conf=null;
            if (hasSelfConfigPath()) {
                conf=new File(getConfigPath()).getPath();
                fp = conf + File.separator + "hsb.properties";
                pidFile = conf + File.separator + "pid";
            }else{
                conf=HeisenbergStartup.class.getResource("/").getPath();
                URL uri = HeisenbergStartup.class.getResource("/hsb.properties");
                fp = uri.getPath();
            }
            System.out.println("hsb.conf-->" + conf);
            if(StringUtil.isNotEmpty(fp)){
                System.out.println("loading properties file["+fp+"]");
                HeisenbergContext.load(fp);
            }

            // init
            HeisenbergServer server = HeisenbergServer.getInstance();
            server.beforeStart(dateFormat);

            // startup
            server.startup();
            //pid
            if(StringUtil.isNotEmpty(pidFile)){
                File _pidFile=new File(pidFile); 
                String[] content={PID,getConfigPath(),System.getProperty("hsb.log.home")};
                if (_pidFile!=null && _pidFile.exists()) {
                    content=FileUtils.readFileToString(_pidFile).split(com.baidu.hsb.route.util.StringUtil.LINE_END);
                    content[0]=PID;
                }
                FileUtils.write(_pidFile, StringUtil.join(content,com.baidu.hsb.route.util.StringUtil.LINE_END));
                System.out.println("PID["+PID+"] has been written!");
            }
            
        } catch (Throwable e) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LogLog.error(sdf.format(new Date()) + " startup error", e);
            System.exit(-1);
        } finally {
            System.out.println("start hsb time:" + (System.currentTimeMillis() - s) + "ms");
        }
    }

}
