/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.hsb.manager.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseClear;
import com.baidu.hsb.manager.response.ClearSlow;
import com.baidu.hsb.util.StringUtil;

/**
 * @author xianmao.hexm 2012-4-16
 */
public class ClearHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        int rs = ManagerParseClear.parse(stmt, offset);
        switch (rs & 0xff) {
        case ManagerParseClear.SLOW_DATANODE: {
            String name = stmt.substring(rs >>> 8).trim();
            if (StringUtil.isEmpty(name)) {
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            } else {
                ClearSlow.dataNode(c, name);
            }
            break;
        }
        case ManagerParseClear.SLOW_SCHEMA: {
            String name = stmt.substring(rs >>> 8).trim();
            if (StringUtil.isEmpty(name)) {
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            } else {
                ClearSlow.schema(c, name);
            }
            break;
        }
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }
}
