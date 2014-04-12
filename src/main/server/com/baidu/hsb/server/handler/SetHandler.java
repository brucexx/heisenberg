/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import static com.baidu.hsb.server.parser.ServerParseSet.AUTOCOMMIT_OFF;
import static com.baidu.hsb.server.parser.ServerParseSet.AUTOCOMMIT_ON;
import static com.baidu.hsb.server.parser.ServerParseSet.CHARACTER_SET_CLIENT;
import static com.baidu.hsb.server.parser.ServerParseSet.CHARACTER_SET_CONNECTION;
import static com.baidu.hsb.server.parser.ServerParseSet.CHARACTER_SET_RESULTS;
import static com.baidu.hsb.server.parser.ServerParseSet.NAMES;
import static com.baidu.hsb.server.parser.ServerParseSet.TX_READ_COMMITTED;
import static com.baidu.hsb.server.parser.ServerParseSet.TX_READ_UNCOMMITTED;
import static com.baidu.hsb.server.parser.ServerParseSet.TX_REPEATED_READ;
import static com.baidu.hsb.server.parser.ServerParseSet.TX_SERIALIZABLE;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.Isolations;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.parser.ServerParseSet;
import com.baidu.hsb.server.response.CharacterSet;

/**
 * SET 语句处理
 * 
 * @author xiongzhao@baidu.com
 */
public final class SetHandler {

    private static final Logger logger = Logger.getLogger(SetHandler.class);
    private static final byte[] AC_OFF = new byte[] { 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 };

    public static void handle(String stmt, ServerConnection c, int offset) {
        int rs = ServerParseSet.parse(stmt, offset);
        switch (rs & 0xff) {
        case AUTOCOMMIT_ON:
            if (c.isAutocommit()) {
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            } else {
                c.commit();
                c.setAutocommit(true);
            }
            break;
        case AUTOCOMMIT_OFF: {
            if (c.isAutocommit()) {
                c.setAutocommit(false);
            }
            c.write(c.writeToBuffer(AC_OFF, c.allocate()));
            break;
        }
        case TX_READ_UNCOMMITTED: {
            c.setTxIsolation(Isolations.READ_UNCOMMITTED);
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            break;
        }
        case TX_READ_COMMITTED: {
            c.setTxIsolation(Isolations.READ_COMMITTED);
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            break;
        }
        case TX_REPEATED_READ: {
            c.setTxIsolation(Isolations.REPEATED_READ);
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            break;
        }
        case TX_SERIALIZABLE: {
            c.setTxIsolation(Isolations.SERIALIZABLE);
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            break;
        }
        case NAMES:
            String charset = stmt.substring(rs >>> 8).trim();
            if (c.setCharset(charset)) {
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
            } else {
                c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
            }
            break;
        case CHARACTER_SET_CLIENT:
        case CHARACTER_SET_CONNECTION:
        case CHARACTER_SET_RESULTS:
            CharacterSet.response(stmt, c, rs);
            break;
        default:
            StringBuilder s = new StringBuilder();
            logger.warn(s.append(c).append(stmt).append(" is not executed").toString());
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        }
    }

}
