/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import com.baidu.hsb.route.util.StringUtil;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.parser.ServerParse;

/**
 * xa事务处理类
 * 
 * @author brucexx
 *
 */
public class XAHandler {

    public static final String START = "START";
    public static final String END = "END";
    public static final String PREPARE = "PREPARE";
    public static final String COMMIT = "COMMIT";
    public static final String RECOVER = "RECOVER";

    /**
     * 用来处理xa事务逻辑
     * 
     * @param stmt
     * @param c
     * @param offs
     */
    public static void handle(String stmt, ServerConnection c, int offs) {
        // 分析操作
        String t = StringUtil.upperCase(stmt);
        String[] ss = StringUtil.split(t, "\\t|\\s+");
        String op = ss[1];
        String xid = StringUtil.replace(ss[2], "'", "");
        switch (op) {
            case START:
            case END:
            case PREPARE:
            case COMMIT:
                // 直接分发
                c.execute(stmt, ServerParse.XA);
                break;
            case RECOVER:
                break;
            default:
                break;
        }

    }

}
