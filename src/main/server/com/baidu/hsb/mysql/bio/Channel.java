/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio;

/**
 * @author xiongzhao@baidu.com 2011-5-5 上午11:44:31
 */
public interface Channel {

    /**
     * 取得最近活跃时间
     */
    long getLastAcitveTime();

    /**
     * 更新最近活跃时间
     */
    void setLastActiveTime(long time);

    /**
     * 连接通道
     */
    void connect(long timeout) throws Exception;

    /**
     * 是否事务自动递交模式
     */
    boolean isAutocommit();

    /**
     * 通道是否正在执行中
     */
    boolean isRunning();

    /**
     * 设置通道是否正在执行
     */
    void setRunning(boolean running);

    /**
     * 将通道释放到数据源池里
     */
    void release();

    /**
     * 检查通道是否已关闭
     */
    boolean isClosed();

    /**
     * {@link #close()} and ensure that the remote side has closed this channel
     * too
     */
    void kill();

    /**
     * 关闭数据通道 (thread-safe)
     */
    void close();

    /**
     * 关闭未激活的数据通道
     */
    void closeNoActive();

}
