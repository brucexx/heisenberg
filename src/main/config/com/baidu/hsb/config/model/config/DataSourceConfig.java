/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.config.model.config;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: DataSourceConfig.java, v 0.1 2013年12月31日 上午10:47:58 HI:brucest0078 Exp $
 */
public final class DataSourceConfig {

    private static final int DEFAULT_SQL_RECORD_COUNT = 10;

    private String           name;
    private String           type;
    private String           host;
    private int              port;
    private String           user;
    private String           password;
    private boolean          needEncrypt              = false;
    private String           database;
    private String           sqlMode;
    private int              sqlRecordCount           = DEFAULT_SQL_RECORD_COUNT;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSqlMode() {
        return sqlMode;
    }

    public void setSqlMode(String sqlMode) {
        this.sqlMode = sqlMode;
    }

    public int getSqlRecordCount() {
        return sqlRecordCount;
    }
    
    

    /**
     * Getter method for property <tt>needEncrypt</tt>.
     * 
     * @return property value of needEncrypt
     */
    public boolean isNeedEncrypt() {
        return needEncrypt;
    }

    /**
     * Setter method for property <tt>needEncrypt</tt>.
     * 
     * @param needEncrypt value to be assigned to property needEncrypt
     */
    public void setNeedEncrypt(boolean needEncrypt) {
        this.needEncrypt = needEncrypt;
    }

    public void setSqlRecordCount(int sqlRecordCount) {
        this.sqlRecordCount = sqlRecordCount;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[name=").append(name).append(",host=").append(host)
            .append(",port=").append(port).append(",database=").append(database).append(']')
            .toString();
    }

}
