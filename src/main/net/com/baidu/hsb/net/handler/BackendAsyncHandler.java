/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.baidu.hsb.net.NIOHandler;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BackendAsyncHandler.java, v 0.1 2013年12月26日 下午6:02:02 HI:brucest0078 Exp $
 */
public abstract class BackendAsyncHandler implements NIOHandler {

    protected final BlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<byte[]>();
    protected final AtomicBoolean isHandling = new AtomicBoolean(false);

    protected void offerData(byte[] data, Executor executor) {
        if (dataQueue.offer(data)) {
            handleQueue(executor);
        } else {
            offerDataError();
        }
    }

    protected abstract void offerDataError();

    protected abstract void handleData(byte[] data);

    protected abstract void handleDataError(Throwable t);

    protected void handleQueue(final Executor executor) {
        if (isHandling.compareAndSet(false, true)) {
            // 异步处理后端数据
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = null;
                        while ((data = dataQueue.poll()) != null) {
                            handleData(data);
                        }
                    } catch (Throwable t) {
                        handleDataError(t);
                    } finally {
                        isHandling.set(false);
                        if (dataQueue.size() > 0) {
                            handleQueue(executor);
                        }
                    }
                }
            });
        }
    }

}
