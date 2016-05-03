/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.common;

import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.heartbeat.MySQLHeartbeat;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.statistic.SQLRecorder;

/**
 * @author brucexx
 *
 */
public interface DataSource {

    
    public MySQLDataNode getNode();
    
    public int getIndex();
    
    public String getName();
    
    public DataSourceConfig getConfig();
    
    public int size() ;
    
    public int getActiveCount();
    
    public int getIdleCount();
    
    public MySQLHeartbeat getHeartbeat();
    
    public SQLRecorder getSqlRecorder() ;
    
    public void startHeartbeat();
    
    public void stopHeartbeat();
    
    public void doHeartbeat();
    
    public void deActive();
    
    public void clear();
    
    public void idleCheck(long timeout);
    
    
}
