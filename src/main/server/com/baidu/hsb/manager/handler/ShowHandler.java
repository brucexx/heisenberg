/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.manager.parser.ManagerParseShow;
import com.baidu.hsb.manager.response.ShowBackend;
import com.baidu.hsb.manager.response.ShowCollation;
import com.baidu.hsb.manager.response.ShowCommand;
import com.baidu.hsb.manager.response.ShowConnection;
import com.baidu.hsb.manager.response.ShowConnectionSQL;
import com.baidu.hsb.manager.response.ShowDataNode;
import com.baidu.hsb.manager.response.ShowDataSource;
import com.baidu.hsb.manager.response.ShowDatabase;
import com.baidu.hsb.manager.response.ShowHeartbeat;
import com.baidu.hsb.manager.response.ShowHelp;
import com.baidu.hsb.manager.response.ShowParser;
import com.baidu.hsb.manager.response.ShowProcessor;
import com.baidu.hsb.manager.response.ShowRouter;
import com.baidu.hsb.manager.response.ShowSQL;
import com.baidu.hsb.manager.response.ShowSQLDetail;
import com.baidu.hsb.manager.response.ShowSQLExecute;
import com.baidu.hsb.manager.response.ShowSQLSlow;
import com.baidu.hsb.manager.response.ShowServer;
import com.baidu.hsb.manager.response.ShowSlow;
import com.baidu.hsb.manager.response.ShowThreadPool;
import com.baidu.hsb.manager.response.ShowTime;
import com.baidu.hsb.manager.response.ShowVariables;
import com.baidu.hsb.manager.response.ShowVersion;
import com.baidu.hsb.parser.util.ParseUtil;
import com.baidu.hsb.util.StringUtil;

/**
 * @author xiongzhao@baidu.com
 */
public final class ShowHandler {

    public static void handle(String stmt, ManagerConnection c, int offset) {
        int rs = ManagerParseShow.parse(stmt, offset);
        switch (rs & 0xff) {
        case ManagerParseShow.COMMAND:
            ShowCommand.execute(c);
            break;
        case ManagerParseShow.COLLATION:
            ShowCollation.execute(c);
            break;
        case ManagerParseShow.CONNECTION:
            ShowConnection.execute(c);
            break;
        case ManagerParseShow.BACKEND:
            ShowBackend.execute(c);
            break;
        case ManagerParseShow.CONNECTION_SQL:
            ShowConnectionSQL.execute(c);
            break;
        case ManagerParseShow.DATABASE:
            ShowDatabase.execute(c);
            break;
        case ManagerParseShow.DATANODE:
            ShowDataNode.execute(c, null);
            break;
        case ManagerParseShow.DATANODE_WHERE: {
            String name = stmt.substring(rs >>> 8).trim();
            if (StringUtil.isEmpty(name)) {
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            } else {
                ShowDataNode.execute(c, name);
            }
            break;
        }
        case ManagerParseShow.DATASOURCE:
            ShowDataSource.execute(c, null);
            break;
        case ManagerParseShow.DATASOURCE_WHERE: {
            String name = stmt.substring(rs >>> 8).trim();
            if (StringUtil.isEmpty(name)) {
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            } else {
                ShowDataSource.execute(c, name);
            }
            break;
        }
        case ManagerParseShow.HELP:
            ShowHelp.execute(c);
            break;
        case ManagerParseShow.HEARTBEAT:
            ShowHeartbeat.response(c);
            break;
        case ManagerParseShow.PARSER:
            ShowParser.execute(c);
            break;
        case ManagerParseShow.PROCESSOR:
            ShowProcessor.execute(c);
            break;
        case ManagerParseShow.ROUTER:
            ShowRouter.execute(c);
            break;
        case ManagerParseShow.SERVER:
            ShowServer.execute(c);
            break;
        case ManagerParseShow.SQL:
            ShowSQL.execute(c, ParseUtil.getSQLId(stmt));
            break;
        case ManagerParseShow.SQL_DETAIL:
            ShowSQLDetail.execute(c, ParseUtil.getSQLId(stmt));
            break;
        case ManagerParseShow.SQL_EXECUTE:
            ShowSQLExecute.execute(c);
            break;
        case ManagerParseShow.SQL_SLOW:
            ShowSQLSlow.execute(c);
            break;
        case ManagerParseShow.SLOW_DATANODE: {
            String name = stmt.substring(rs >>> 8).trim();
            if (StringUtil.isEmpty(name)) {
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            } else {
                ShowSlow.dataNode(c, name);
            }
            break;
        }
        case ManagerParseShow.SLOW_SCHEMA: {
            String name = stmt.substring(rs >>> 8).trim();
            if (StringUtil.isEmpty(name)) {
                c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
            } else {
                ShowSlow.schema(c, name);
            }
            break;
        }
        case ManagerParseShow.THREADPOOL:
            ShowThreadPool.execute(c);
            break;
        case ManagerParseShow.TIME_CURRENT:
            ShowTime.execute(c, ManagerParseShow.TIME_CURRENT);
            break;
        case ManagerParseShow.TIME_STARTUP:
            ShowTime.execute(c, ManagerParseShow.TIME_STARTUP);
            break;
        case ManagerParseShow.VARIABLES:
            ShowVariables.execute(c);
            break;
        case ManagerParseShow.VERSION:
            ShowVersion.execute(c);
            break;
        default:
            c.writeErrMessage(ErrorCode.ER_YES, "Unsupported statement");
        }
    }
}
