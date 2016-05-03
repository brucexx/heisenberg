/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.baidu.hsb.net.AbstractConnection;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BufferQueue.java, v 0.1 2013年12月26日 下午6:01:47 HI:brucest0078 Exp $
 */
public final class BufferQueue {

    protected static final Logger LOGGER = Logger.getLogger(BufferQueue.class);

    private int takeIndex;
    private int putIndex;
    private int count;
    private final ByteBuffer[] items;
    private final ReentrantLock lock;
    private final Condition notFull;
    private ByteBuffer attachment;

    public BufferQueue(int capacity) {
        items = new ByteBuffer[capacity];
        lock = new ReentrantLock();
        notFull = lock.newCondition();
    }

    public ByteBuffer attachment() {
        return attachment;
    }

    public void attach(ByteBuffer buffer) {
        this.attachment = buffer;
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public void put(ByteBuffer buffer) throws InterruptedException {
        final ByteBuffer[] items = this.items;
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            try {
                while (count == items.length) {
                    notFull.await();
                }
            } catch (InterruptedException ie) {
                notFull.signal();
                throw ie;
            }
            insert(buffer);
        } finally {
            lock.unlock();
        }
    }

    public ByteBuffer poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (count == 0) {
                return null;
            }

            return extract();
        } finally {
            lock.unlock();
        }
    }

    private void insert(ByteBuffer buffer) {
      
        items[putIndex] = buffer;
        putIndex = inc(putIndex);
        ++count;
        if (buffer != null && buffer.position()==0) {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("insert empty buffer["+putIndex+"]...");
            }
        }
    }

    private ByteBuffer extract() {
        final ByteBuffer[] items = this.items;
        ByteBuffer buffer = items[takeIndex];
//        if (LOGGER.isDebugEnabled()) {
//            StringBuffer sb = new StringBuffer();
//            int i = 0;
//            for (ByteBuffer bf : items) {
//                i++;
//                if (bf != null)
//                    sb.append("(" + i + ")[" + bf.position() + "],");
//            }
//            LOGGER.debug("remain buf:" + sb.toString());
//
//            if (buffer != null && buffer.position() == 0) {
//                LOGGER.debug("poll buffer is empty");
//            }
//        }
        items[takeIndex] = null;
        takeIndex = inc(takeIndex);
        --count;
        notFull.signal();
        return buffer;
    }

    private int inc(int i) {
        return (++i == items.length) ? 0 : i;
    }

}
