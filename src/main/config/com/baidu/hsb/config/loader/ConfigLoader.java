/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * (created at 2012-6-13)
 */
package com.baidu.hsb.config.loader;

import java.util.Map;
import java.util.Set;

import com.baidu.hsb.config.model.config.ClusterConfig;
import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.QuarantineConfig;
import com.baidu.hsb.config.model.config.RuleConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.config.model.config.UserConfig;
import com.baidu.hsb.config.model.rule.RuleAlgorithm;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
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
