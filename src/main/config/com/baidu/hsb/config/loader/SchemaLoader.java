/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.loader;

import java.util.Map;

import com.baidu.hsb.config.model.config.DataNodeConfig;
import com.baidu.hsb.config.model.config.DataSourceConfig;
import com.baidu.hsb.config.model.config.SchemaConfig;
import com.baidu.hsb.config.model.config.TableRuleConfig;

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
