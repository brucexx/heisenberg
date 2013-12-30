/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.handler;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.NIOHandler;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.statistic.CommandCount;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: FrontendCommandHandler.java, v 0.1 2013年12月26日 下午6:02:12 HI:brucest0078 Exp $
 */
public class FrontendCommandHandler implements NIOHandler {

    protected final FrontendConnection source;
    protected final CommandCount commands;

    public FrontendCommandHandler(FrontendConnection source) {
        this.source = source;
        this.commands = source.getProcessor().getCommands();
    }

    @Override
    public void handle(byte[] data) {
        switch (data[4]) {
        case MySQLPacket.COM_INIT_DB:
            commands.doInitDB();
            source.initDB(data);
            break;
        case MySQLPacket.COM_QUERY:
            commands.doQuery();
            source.query(data);
            break;
        case MySQLPacket.COM_PING:
            commands.doPing();
            source.ping();
            break;
        case MySQLPacket.COM_QUIT:
            commands.doQuit();
            source.close();
            break;
        case MySQLPacket.COM_PROCESS_KILL:
            commands.doKill();
            source.kill(data);
            break;
        case MySQLPacket.COM_STMT_PREPARE:
            commands.doStmtPrepare();
            source.stmtPrepare(data);
            break;
        case MySQLPacket.COM_STMT_EXECUTE:
            commands.doStmtExecute();
            source.stmtExecute(data);
            break;
        case MySQLPacket.COM_STMT_CLOSE:
            commands.doStmtClose();
            source.stmtClose(data);
            break;
        case MySQLPacket.COM_HEARTBEAT:
            commands.doHeartbeat();
            source.heartbeat(data);
            break;
        default:
            commands.doOther();
            source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
        }
    }

}
