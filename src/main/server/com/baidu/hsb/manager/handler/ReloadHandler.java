/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseReload;
import com.baidu.hsb.manager.response.ReloadConfig;
import com.baidu.hsb.manager.response.ReloadUser;

/**
 * @author xiongzhao@baidu.com
 */
public final class ReloadHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        int rs = ManagerParseReload.parse(stmt, offset);
        switch (rs) {
        case ManagerParseReload.CONFIG:
            ReloadConfig.execute(c);
            break;
        case ManagerParseReload.ROUTE:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            break;
        case ManagerParseReload.USER:
            ReloadUser.execute(c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
