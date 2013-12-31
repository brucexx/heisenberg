/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseRollback;
import com.baidu.hsb.manager.response.RollbackConfig;
import com.baidu.hsb.manager.response.RollbackUser;

/**
 * @author xiongzhao@baidu.com
 */
public final class RollbackHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseRollback.parse(stmt, offset)) {
        case ManagerParseRollback.CONFIG:
            RollbackConfig.execute(c);
            break;
        case ManagerParseRollback.ROUTE:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            break;
        case ManagerParseRollback.USER:
            RollbackUser.execute(c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
