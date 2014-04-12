/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.manager.response;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergCluster;
import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.ConfigInitializer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.QuarantineConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.UserConfig;
import com.baidu.hsb.manager.ManagerConnection;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.MySQLDataSource;
import com.baidu.hsb.net.mysql.OkPacket;

/**
 * @author xiongzhao@baidu.com
 */
public final class ReloadConfig {
    private static final Logger LOGGER = Logger.getLogger(ReloadConfig.class);

    public static void execute(ManagerConnection c) {
        final ReentrantLock lock = HeisenbergServer.getInstance().getConfig().getLock();
        lock.lock();
        try {
            if (reload()) {
                StringBuilder s = new StringBuilder();
                s.append(c).append("Reload config success by manager");
                LOGGER.warn(s.toString());
                OkPacket ok = new OkPacket();
                ok.packetId = 1;
                ok.affectedRows = 1;
                ok.serverStatus = 2;
                ok.message = "Reload config success".getBytes();
                ok.write(c);
            } else {
                c.writeErrMessage(ErrorCode.ER_YES, "Reload config failure");
            }
        } finally {
            lock.unlock();
        }
    }

    private static boolean reload() {
        // 载入新的配置
        ConfigInitializer loader = new ConfigInitializer();
        Map<String, UserConfig> users = loader.getUsers();
        Map<String, SchemaConfig> schemas = loader.getSchemas();
        Map<String, MySQLDataNode> dataNodes = loader.getDataNodes();
        Map<String, DataSourceConfig> dataSources = loader.getDataSources();
        HeisenbergCluster cluster = loader.getCluster();
        QuarantineConfig quarantine = loader.getQuarantine();

        // 应用新配置
        HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();

        // 如果重载已经存在的数据节点，初始化连接数参考空闲连接数，否则为1。
        boolean reloadStatus = true;
        Map<String, MySQLDataNode> cNodes = conf.getDataNodes();
        for (MySQLDataNode dn : dataNodes.values()) {
            MySQLDataNode cdn = cNodes.get(dn.getName());
            if (cdn != null && cdn.getSource() != null) {
                int size = Math.min(cdn.getSource().getIdleCount(), dn.getConfig().getPoolSize());
                dn.init(size > 0 ? size : 1, 0);
            } else {
                dn.init(1, 0);
            }
            if (!dn.isInitSuccess()) {
                reloadStatus = false;
                break;
            }
        }
        // 如果重载不成功，则清理已初始化的资源。
        if (!reloadStatus) {
            for (MySQLDataNode dn : dataNodes.values()) {
                MySQLDataSource ds = dn.getSource();
                if (ds != null) {
                    ds.clear();
                }
            }
            return false;
        }

        // 应用重载
        conf.reload(users, schemas, dataNodes, dataSources, cluster, quarantine);

        // 处理旧的资源
        for (MySQLDataNode dn : cNodes.values()) {
            MySQLDataSource ds = dn.getSource();
            if (ds != null) {
                ds.clear();
            }
        }

        return true;
    }

}
