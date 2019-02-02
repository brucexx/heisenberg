/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.server.session;

/**
 * @author brucexx
 *
 */
public interface FailCondCallback {

    /**
     * 前置条件
     */
    void condition(int code, String msg);

}
