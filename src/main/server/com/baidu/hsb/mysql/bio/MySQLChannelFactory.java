/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio;

import com.baidu.hsb.mysql.MySQLDataSource;

/**
 * @author xiongzhao@baidu.com 2011-5-6 下午03:10:16
 */
public final class MySQLChannelFactory implements ChannelFactory {

    @Override
    public Channel make(MySQLDataSource dataSource) {
        return new MySQLChannel(dataSource);
    }

}
