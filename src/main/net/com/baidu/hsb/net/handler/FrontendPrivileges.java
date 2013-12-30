/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.handler;

import java.util.Set;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: FrontendPrivileges.java, v 0.1 2013年12月26日 下午6:02:23 HI:brucest0078 Exp $
 */
public interface FrontendPrivileges {

    /**
     * 检查schema是否存在
     */
    boolean schemaExists(String schema);

    /**
     * 检查用户是否存在，并且可以使用host实行隔离策略。
     */
    boolean userExists(String user, String host);

    /**
     * 提供用户的服务器端密码
     */
    String getPassword(String user);

    /**
     * 提供有效的用户schema集合
     */
    Set<String> getUserSchemas(String user);

}
