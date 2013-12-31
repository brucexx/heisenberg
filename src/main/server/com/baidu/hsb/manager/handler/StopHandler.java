/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseStop;
import com.baidu.hsb.manager.response.StopHeartbeat;

/**
 * @author xiongzhao@baidu.com
 */
public final class StopHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseStop.parse(stmt, offset)) {
        case ManagerParseStop.HEARTBEAT:
            StopHeartbeat.execute(stmt, c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
