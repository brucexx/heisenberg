/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.Alarms;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.nio.handler.DelegateResponseHandler;
import com.baidu.hsb.mysql.nio.handler.ResponseHandler;
import com.baidu.hsb.statistic.SQLRecorder;
import com.baidu.hsb.util.TimeUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLConnectionPool {
    private static final Logger alarm = Logger.getLogger("alarm");

    private final MySQLDataNode dataNode;
    private final int index;
    private final String name;
    private final ReentrantLock lock = new ReentrantLock();
    private final MySQLConnectionFactory factory;
    private final DataSourceConfig config;
    private final int size;

    private final MySQLConnection[] items;
    private int activeCount;
    private int idleCount;
    private final SQLRecorder sqlRecorder;

    public MySQLConnectionPool(MySQLDataNode node, int index, DataSourceConfig config, int size) {
        this.dataNode = node;
        this.size = size;
        this.items = new MySQLConnection[size];
        this.config = config;
        this.name = config.getName();
        this.index = index;
        this.factory = new MySQLConnectionFactory();
        this.sqlRecorder = new SQLRecorder(config.getSqlRecordCount());
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void getConnection(final ResponseHandler handler, final Object attachment) throws Exception {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // too many active connections
            if (activeCount >= size) {
                StringBuilder s = new StringBuilder();
                s.append(Alarms.DEFAULT).append("[name=").append(name).append(",active=");
                s.append(activeCount).append(",size=").append(size).append(']');
                alarm.error(s.toString());
            }

            // get connection from pool
            final MySQLConnection[] items = this.items;
            for (int i = 0, len = items.length; idleCount > 0 && i < len; ++i) {
                if (items[i] != null) {
                    MySQLConnection conn = items[i];
                    items[i] = null;
                    --idleCount;
                    if (conn.isClosedOrQuit()) {
                        continue;
                    } else {
                        ++activeCount;
                        conn.setAttachment(attachment);
                        handler.connectionAcquired(conn);
                        return;
                    }
                }
            }

            ++activeCount;
        } finally {
            lock.unlock();
        }

        // create connection
        factory.make(this, new DelegateResponseHandler(handler) {
            private boolean deactived;

            @Override
            public void connectionError(Throwable e, MySQLConnection conn) {
                lock.lock();
                try {
                    if (!deactived) {
                        --activeCount;
                        deactived = true;
                    }
                } finally {
                    lock.unlock();
                }
                handler.connectionError(e, conn);
            }

            @Override
            public void connectionAcquired(MySQLConnection conn) {
                conn.setAttachment(attachment);
                handler.connectionAcquired(conn);
            }
        });
    }

    public void releaseChannel(MySQLConnection c) {
        if (c == null || c.isClosedOrQuit()) {
            return;
        }

        // release connection
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final MySQLConnection[] items = this.items;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    ++idleCount;
                    --activeCount;
                    c.setLastTime(TimeUtil.currentTimeMillis());
                    items[i] = c;
                    return;
                }
            }
        } finally {
            lock.unlock();
        }

        // close excess connection
        c.quit();
    }

    public void deActive() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            --activeCount;
        } finally {
            lock.unlock();
        }
    }

    public SQLRecorder getSqlRecorder() {
        return sqlRecorder;
    }

    public DataSourceConfig getConfig() {
        return config;
    }

}
