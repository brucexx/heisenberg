/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import com.baidu.hsb.route.util.StringUtil;

/**
 * @author brucexx
 *
 */
public class XAUtil {

    public static boolean isXaStart(String sql) {
        String s = StringUtil.lowerCase(sql);
        return StringUtil.contains(s, "xa") && StringUtil.contains(s, "start");
    }

    public static boolean isXAOp(String sql) {
        String s = StringUtil.lowerCase(sql);
        return StringUtil.contains(s, "xa");
    }

}
