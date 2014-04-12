/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BufferQueue.java, v 0.1 2013年12月26日 下午6:01:47 HI:brucest0078 Exp $
 */
public final class BufferQueue {

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
    }

    private ByteBuffer extract() {
        final ByteBuffer[] items = this.items;
        ByteBuffer buffer = items[takeIndex];
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
