/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.nio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.baidu.hsb.config.Alarms;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.heartbeat.MySQLHeartbeat;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.common.DataSource;
import com.baidu.hsb.mysql.nio.handler.DelegateResponseHandler;
import com.baidu.hsb.mysql.nio.handler.ResponseHandler;
import com.baidu.hsb.statistic.SQLRecorder;
import com.baidu.hsb.util.TimeUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class MySQLConnectionPool implements DataSource {
    private static final Logger LOGGER = Logger.getLogger(MySQLConnectionPool.class);

    private static final Logger alarm = Logger.getLogger("alarm");

    private final MySQLDataNode dataNode;
    private final int index;
    private final String name;
    private final ReentrantLock lock = new ReentrantLock();
    private final DataSourceConfig config;
    private final int size;

    private final MySQLConnectionFactory factory;
    private final MySQLConnection[] items;
    private AtomicInteger activeCount=new AtomicInteger(0);
    private AtomicInteger idleCount=new AtomicInteger(0);
    private final SQLRecorder sqlRecorder;
    private final MySQLHeartbeat heartbeat;

    public MySQLConnectionPool(MySQLDataNode node, int index, DataSourceConfig config, int size) {
        this.dataNode = node;
        this.size = size;
        this.config = config;
        this.name = config.getName();
        this.index = index;
        this.items = new MySQLConnection[size];
        this.factory = new MySQLConnectionFactory();
        this.heartbeat = new MySQLHeartbeat(this);
        this.sqlRecorder = new SQLRecorder(config.getSqlRecordCount());
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public MySQLConnection getConnection(final ResponseHandler handler, final Object attachment) throws Exception {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // too many active connections
            if (activeCount.get()+idleCount.get() >= size) {
                StringBuilder s = new StringBuilder();
                s.append(Alarms.DEFAULT).append("[name=").append(name).append(",active=");
                s.append(activeCount).append(",size=").append(size).append(']');
                alarm.error(s.toString());
            }

            // get connection from pool
            final MySQLConnection[] items = this.items;
            for (int i = 0, len = items.length; idleCount.get() > 0 && i < len; ++i) {
                if (items[i] != null&& !items[i].isRunning() ) {
                    MySQLConnection conn = items[i];
                    items[i] = null;
                    idleCount.decrementAndGet();
                    if (conn.isClosedOrQuit()) {
                        continue;
                    } else {
                        activeCount.incrementAndGet();
                        conn.setAttachment(attachment);
                        handler.connectionAcquired(conn);
                        return conn;
                    }
                }
            }
            activeCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
        final List<MySQLConnection> holder = new ArrayList<MySQLConnection>();

        // create connection
        factory.make(this, new DelegateResponseHandler(handler) {
            private boolean deactived;

            @Override
            public void connectionError(Throwable e, MySQLConnection conn) {
                lock.lock();
                try {
                    if (!deactived) {
                        activeCount.decrementAndGet();
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
                holder.add(conn);
            }
        });
        return CollectionUtils.isEmpty(holder) ? null : holder.get(0);
    }

    /**
     * 仅release 但是不关闭
     * @param c
     */
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
                    idleCount.incrementAndGet();
                    activeCount.decrementAndGet();
                    c.setLastActiveTime(TimeUtil.currentTimeMillis());
                    items[i] = c;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(getName() + "[" + getIndex() + "] activeCount[" + activeCount + "]idleCount["
                                + idleCount + "]release connection-->" + i);
                    }
                    return;
                }
            }
        } finally {
            lock.unlock();
        }

        // close excess connection
        // c.quit();
    }

    public void deActive() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            activeCount.decrementAndGet();
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

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#getNode()
     */
    @Override
    public MySQLDataNode getNode() {
        return dataNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#size()
     */
    @Override
    public int size() {
        return size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#getActiveCount()
     */
    @Override
    public int getActiveCount() {
        return activeCount.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#getIdleCount()
     */
    @Override
    public int getIdleCount() {
        return idleCount.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#getHeartbeat()
     */
    @Override
    public MySQLHeartbeat getHeartbeat() {
        return heartbeat;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#startHeartbeat()
     */
    @Override
    public void startHeartbeat() {
        heartbeat.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#stopHeartbeat()
     */
    @Override
    public void stopHeartbeat() {
        heartbeat.stop();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#doHeartbeat()
     */
    @Override
    public void doHeartbeat() {
        if (!heartbeat.isStop()) {
            try {
                heartbeat.heartbeat();
            } catch (Throwable e) {
                LOGGER.error(name + " heartbeat error.", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#clear()
     */
    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final MySQLConnection[] items = this.items;
            for (int i = 0; i < items.length; i++) {
                MySQLConnection c = items[i];
                if (c != null) {
                    c.closeNoActive();
                    idleCount.decrementAndGet();
                    items[i] = null;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.hsb.mysql.common.DataSource#idleCheck(long)
     */
    @Override
    public void idleCheck(long timeout) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final MySQLConnection[] items = this.items;
            long time = TimeUtil.currentTimeMillis() - timeout;
            for (int i = 0; i < items.length; i++) {
                MySQLConnection c = items[i];
                if (c != null && time > c.getLastActiveTime()) {
                    c.closeNoActive();
                    idleCount.decrementAndGet();
                    items[i] = null;
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
