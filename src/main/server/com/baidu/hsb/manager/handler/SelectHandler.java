/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.handler;

import static com.baidu.hsb.manager.parser.ManagerParseSelect.SESSION_AUTO_INCREMENT;
import static com.baidu.hsb.manager.parser.ManagerParseSelect.VERSION_COMMENT;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseSelect;
import com.baidu.hsb.manager.response.SelectSessionAutoIncrement;
import com.baidu.hsb.manager.response.SelectVersionComment;

/**
 * @author xiongzhao@baidu.com
 */
public final class SelectHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        switch (ManagerParseSelect.parse(stmt, offset)) {
        case VERSION_COMMENT:
            SelectVersionComment.execute(c);
            break;
        case SESSION_AUTO_INCREMENT:
            SelectSessionAutoIncrement.execute(c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }

}
