/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.handler;

import static com.baidu.hsb.manager.parser.ManagerParseSwitch.DATASOURCE;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseSwitch;
import com.baidu.hsb.manager.response.SwitchDataSource;

/**
 * @author xiongzhao@baidu.com
 */
public final class SwitchHandler {

    public static void handler(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseSwitch.parse(stmt, offset)) {
        case DATASOURCE:
            SwitchDataSource.response(stmt, c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
