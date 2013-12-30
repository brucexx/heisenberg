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
 * (created at 2012-6-19)
 */
package com.baidu.hsb.config.loader;

import java.util.Map;

import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.TableRuleConfig;
import com.baidu.hsb.config.model.rule.RuleAlgorithm;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: SchemaLoader.java, v 0.1 2013年12月21日 下午12:53:29 HI:brucest0078 Exp $
 */
public interface SchemaLoader {
    Map<String, TableRuleConfig> getTableRules();

    Map<String, DataSourceConfig> getDataSources();

    Map<String, DataNodeConfig> getDataNodes();

    Map<String, SchemaConfig> getSchemas();

}
