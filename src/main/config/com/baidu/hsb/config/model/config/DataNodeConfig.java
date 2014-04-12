/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: DataNodeConfig.java, v 0.1 2013年12月31日 上午10:47:48 HI:brucest0078 Exp $
 */
public final class DataNodeConfig {

    private static final int  DEFAULT_POOL_SIZE         = 128;
    private static final long DEFAULT_WAIT_TIMEOUT      = 10 * 1000L;
    private static final long DEFAULT_IDLE_TIMEOUT      = 10 * 60 * 1000L;
    private static final long DEFAULT_HEARTBEAT_TIMEOUT = 30 * 1000L;
    private static final int  DEFAULT_HEARTBEAT_RETRY   = 10;

    private String            name;
    private String            dataSource;
    private int               poolSize                  = DEFAULT_POOL_SIZE;        // 保持后端数据通道的默认最大值
    private long              waitTimeout               = DEFAULT_WAIT_TIMEOUT;     // 取得新连接的等待超时时间
    private long              idleTimeout               = DEFAULT_IDLE_TIMEOUT;     // 连接池中连接空闲超时时间

    // heartbeat config
    private long              heartbeatTimeout          = DEFAULT_HEARTBEAT_TIMEOUT; // 心跳超时时间
    private int               heartbeatRetry            = DEFAULT_HEARTBEAT_RETRY;  // 检查连接发生异常到切换，重试次数
    private String            heartbeatSQL;                                         // 静态心跳语句
    private int               masterReadWeight;
    private int               slaveReadWeight;
    private boolean           needWR                    = false;

    /**
     * Getter method for property <tt>needWR</tt>.
     * 
     * @return property value of needWR
     */
    public boolean isNeedWR() {
        return needWR;
    }

    /**
     * Setter method for property <tt>needWR</tt>.
     * 
     * @param needWR value to be assigned to property needWR
     */
    public void setNeedWR(boolean needWR) {
        this.needWR = needWR;
    }

    public String getHeartbeatSQL() {
        return heartbeatSQL;
    }

    public void setHeartbeatSQL(String heartbeatSQL) {
        this.heartbeatSQL = heartbeatSQL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public int getHeartbeatRetry() {
        return heartbeatRetry;
    }

    public void setHeartbeatRetry(int heartbeatRetry) {
        this.heartbeatRetry = heartbeatRetry;
    }

    public boolean isNeedHeartbeat() {
        return heartbeatSQL != null;
    }

    /**
     * Getter method for property <tt>masterReadWeight</tt>.
     * 
     * @return property value of masterReadWeight
     */
    public int getMasterReadWeight() {
        return masterReadWeight;
    }

    /**
     * Setter method for property <tt>masterReadWeight</tt>.
     * 
     * @param masterReadWeight value to be assigned to property masterReadWeight
     */
    public void setMasterReadWeight(int masterReadWeight) {
        this.masterReadWeight = masterReadWeight;
    }

    /**
     * Getter method for property <tt>slaveReadWeight</tt>.
     * 
     * @return property value of slaveReadWeight
     */
    public int getSlaveReadWeight() {
        return slaveReadWeight;
    }

    /**
     * Setter method for property <tt>slaveReadWeight</tt>.
     * 
     * @param slaveReadWeight value to be assigned to property slaveReadWeight
     */
    public void setSlaveReadWeight(int slaveReadWeight) {
        this.slaveReadWeight = slaveReadWeight;
    }
    
    

}
