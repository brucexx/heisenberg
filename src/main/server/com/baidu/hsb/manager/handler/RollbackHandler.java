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
import com.baidu.hsb.manager.parser.ManagerParseRollback;
import com.baidu.hsb.manager.response.RollbackConfig;
import com.baidu.hsb.manager.response.RollbackUser;

/**
 * @author xianmao.hexm
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
