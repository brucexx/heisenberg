/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.route.context;

import java.util.Map;

import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.mysql.MySQLDataNode;

/**
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: HeisenbergContext.java, v 0.1 2014年3月6日 上午11:38:11 HI:brucest0078 Exp $
 */
public class HeisenbergContext {

    public static Map<String, MySQLDataNode> getMysqlDataNode() {
        return HeisenbergServer.getInstance().getConfig().getDataNodes();
    }

}
