/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.parser.ServerParse;
import com.baidu.hsb.server.parser.ServerParseStart;

/**
 * @author xiongzhao@baidu.com
 */
public final class StartHandler {

    public static void handle(String stmt, ServerConnection c, int offset) {
        switch (ServerParseStart.parse(stmt, offset)) {
        case ServerParseStart.TRANSACTION:
            c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
            break;
        default:
            c.execute(stmt, ServerParse.START);
        }
    }

}
