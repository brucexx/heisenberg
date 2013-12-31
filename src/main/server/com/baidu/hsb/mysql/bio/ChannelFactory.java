/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio;

import com.baidu.hsb.mysql.MySQLDataSource;

/**
 * @author xiongzhao@baidu.com 2011-5-6 下午12:32:55
 */
public interface ChannelFactory {

    Channel make(MySQLDataSource dataSource);

}
