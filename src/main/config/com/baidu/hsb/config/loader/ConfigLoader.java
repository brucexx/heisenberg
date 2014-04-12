/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.loader;

import java.util.Map;

import com.baidu.hsb.config.model.config.ClusterConfig;
import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.QuarantineConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.config.model.config.UserConfig;

/**
 * @author xiongzhao@baidu.com
 */
public interface ConfigLoader {
    //    Map<String, RuleAlgorithm> getRuleFunction();

    // Set<RuleConfig> listRuleConfig();

    SchemaConfig getSchemaConfig(String schema);

    Map<String, SchemaConfig> getSchemaConfigs();

    Map<String, DataNodeConfig> getDataNodes();

    Map<String, DataSourceConfig> getDataSources();

    SystemConfig getSystemConfig();

    UserConfig getUserConfig(String user);

    Map<String, UserConfig> getUserConfigs();

    QuarantineConfig getQuarantineConfig();

    ClusterConfig getClusterConfig();
}
