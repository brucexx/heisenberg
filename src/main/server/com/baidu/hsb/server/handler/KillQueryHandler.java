/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.server.handler;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.NIOProcessor;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.util.StringUtil;

/**
 * @author xiongzhao@baidu.com 2012-4-17
 */
public class KillQueryHandler {

    public static void handle(String stmt, int offset, final ServerConnection c) {
        String id = stmt.substring(offset).trim();
        if (StringUtil.isEmpty(id)) {
            c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "NULL connection id");
        } else {
            // get value
            long value = 0;
            try {
                value = Long.parseLong(id);
            } catch (NumberFormatException e) {
                c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "Invalid connection id:" + id);
                return;
            }

            // kill query itself
            if (value == c.getId()) {
                c.cancel(null);
                return;
            }

            // get the connection and kill query
            FrontendConnection fc = null;
            NIOProcessor[] processors = HeisenbergServer.getInstance().getProcessors();
            for (NIOProcessor p : processors) {
                if ((fc = p.getFrontends().get(value)) != null) {
                    break;
                }
            }
            if (fc != null) {
                if (fc instanceof ServerConnection) {
                    ((ServerConnection) fc).cancel(c);
                } else {
                    c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
                }
            } else {
                c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "Unknown connection id:" + id);
            }
        }
    }

}
