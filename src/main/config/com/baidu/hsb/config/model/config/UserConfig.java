/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

import java.util.Set;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: UserConfig.java, v 0.1 2013年12月31日 上午10:50:22 HI:brucest0078 Exp $
 */
public class UserConfig {

    private String name;
    private String password;
    private Set<String> schemas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

}
