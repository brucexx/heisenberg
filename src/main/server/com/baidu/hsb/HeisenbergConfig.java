/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.QuarantineConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.config.model.config.UserConfig;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.util.TimeUtil;

/**
 * @author xiongzhao@baidu.com
 */
public class HeisenbergConfig {
    private static final int RELOAD = 1;
    private static final int ROLLBACK = 2;

    private volatile SystemConfig system;
    private volatile HeisenbergCluster cluster;
    private volatile HeisenbergCluster _cluster;
    private volatile QuarantineConfig quarantine;
    private volatile QuarantineConfig _quarantine;
    private volatile Map<String, UserConfig> users;
    private volatile Map<String, UserConfig> _users;
    private volatile Map<String, SchemaConfig> schemas;
    private volatile Map<String, SchemaConfig> _schemas;
    private volatile Map<String, MySQLDataNode> dataNodes;
    private volatile Map<String, MySQLDataNode> _dataNodes;
    private volatile Map<String, DataSourceConfig> dataSources;
    private volatile Map<String, DataSourceConfig> _dataSources;
    private long reloadTime;
    private long rollbackTime;
    private int status;
    private final ReentrantLock lock;

    public HeisenbergConfig() {
        ConfigInitializer confInit = new ConfigInitializer();
        this.system = confInit.getSystem();
        this.users = confInit.getUsers();
        this.schemas = confInit.getSchemas();
        this.dataSources = confInit.getDataSources();
        this.dataNodes = confInit.getDataNodes();
        this.quarantine = confInit.getQuarantine();
        this.cluster = confInit.getCluster();

        this.reloadTime = TimeUtil.currentTimeMillis();
        this.rollbackTime = -1L;
        this.status = RELOAD;
        this.lock = new ReentrantLock();
    }

    public SystemConfig getSystem() {
        return system;
    }

    public Map<String, UserConfig> getUsers() {
        return users;
    }

    public Map<String, UserConfig> getBackupUsers() {
        return _users;
    }

    public Map<String, SchemaConfig> getSchemas() {
        return schemas;
    }

    public Map<String, SchemaConfig> getBackupSchemas() {
        return _schemas;
    }

    public Map<String, MySQLDataNode> getDataNodes() {
        return dataNodes;
    }

    public Map<String, MySQLDataNode> getBackupDataNodes() {
        return _dataNodes;
    }

    public Map<String, DataSourceConfig> getDataSources() {
        return dataSources;
    }

    public Map<String, DataSourceConfig> getBackupDataSources() {
        return _dataSources;
    }

    public HeisenbergCluster getCluster() {
        return cluster;
    }

    public HeisenbergCluster getBackupCluster() {
        return _cluster;
    }

    public QuarantineConfig getQuarantine() {
        return quarantine;
    }

    public QuarantineConfig getBackupQuarantine() {
        return _quarantine;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public long getReloadTime() {
        return reloadTime;
    }

    public long getRollbackTime() {
        return rollbackTime;
    }

    public void reload(Map<String, UserConfig> users, Map<String, SchemaConfig> schemas,
            Map<String, MySQLDataNode> dataNodes, Map<String, DataSourceConfig> dataSources, HeisenbergCluster cluster,
            QuarantineConfig quarantine) {
        apply(users, schemas, dataNodes, dataSources, cluster, quarantine);
        this.reloadTime = TimeUtil.currentTimeMillis();
        this.status = RELOAD;
    }

    public boolean canRollback() {
        if (_users == null || _schemas == null || _dataNodes == null || _dataSources == null || _cluster == null
                || _quarantine == null || status == ROLLBACK) {
            return false;
        } else {
            return true;
        }
    }

    public void rollback(Map<String, UserConfig> users, Map<String, SchemaConfig> schemas,
            Map<String, MySQLDataNode> dataNodes, Map<String, DataSourceConfig> dataSources, HeisenbergCluster cluster,
            QuarantineConfig quarantine) {
        apply(users, schemas, dataNodes, dataSources, cluster, quarantine);
        this.rollbackTime = TimeUtil.currentTimeMillis();
        this.status = ROLLBACK;
    }

    private void apply(Map<String, UserConfig> users, Map<String, SchemaConfig> schemas,
            Map<String, MySQLDataNode> dataNodes, Map<String, DataSourceConfig> dataSources, HeisenbergCluster cluster,
            QuarantineConfig quarantine) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // stop mysql heartbeat
            Map<String, MySQLDataNode> oldDataNodes = this.dataNodes;
            if (oldDataNodes != null) {
                for (MySQLDataNode n : oldDataNodes.values()) {
                    if (n != null) {
                        n.stopHeartbeat();
                    }
                }
            }
            // stop cobar heartbeat
            HeisenbergCluster oldCluster = this.cluster;
            if (oldCluster != null) {
                Map<String, HeisenbergNode> nodes = oldCluster.getNodes();
                for (HeisenbergNode n : nodes.values()) {
                    if (n != null) {
                        n.stopHeartbeat();
                    }
                }
            }
            this._users = this.users;
            this._schemas = this.schemas;
            this._dataNodes = this.dataNodes;
            this._dataSources = this.dataSources;
            this._cluster = this.cluster;
            this._quarantine = this.quarantine;

            // start mysql heartbeat
            if (dataNodes != null) {
                for (MySQLDataNode n : dataNodes.values()) {
                    if (n != null) {
                        n.startHeartbeat();
                    }
                }
            }
            // start cobar heartbeat
            if (cluster != null) {
                Map<String, HeisenbergNode> nodes = cluster.getNodes();
                for (HeisenbergNode n : nodes.values()) {
                    if (n != null) {
                        n.startHeartbeat();
                    }
                }
            }
            this.users = users;
            this.schemas = schemas;
            this.dataNodes = dataNodes;
            this.dataSources = dataSources;
            this.cluster = cluster;
            this.quarantine = quarantine;
        } finally {
            lock.unlock();
        }
    }

}
