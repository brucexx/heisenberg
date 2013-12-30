/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.factory;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.baidu.hsb.net.FrontendConnection;
import com.baidu.hsb.net.buffer.BufferQueue;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: FrontendConnectionFactory.java, v 0.1 2013年12月26日 下午6:01:57 HI:brucest0078 Exp $
 */
public abstract class FrontendConnectionFactory {

    protected int socketRecvBuffer = 8 * 1024;
    protected int socketSendBuffer = 16 * 1024;
    protected int packetHeaderSize = 4;
    protected int maxPacketSize = 16 * 1024 * 1024;
    protected int writeQueueCapcity = 16;
    protected long idleTimeout = 8 * 3600 * 1000L;
    protected String charset = "utf8";

    protected abstract FrontendConnection getConnection(SocketChannel channel);

    public FrontendConnection make(SocketChannel channel) throws IOException {
        Socket socket = channel.socket();
        socket.setReceiveBufferSize(socketRecvBuffer);
        socket.setSendBufferSize(socketSendBuffer);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        FrontendConnection c = getConnection(channel);
        c.setPacketHeaderSize(packetHeaderSize);
        c.setMaxPacketSize(maxPacketSize);
        c.setWriteQueue(new BufferQueue(writeQueueCapcity));
        c.setIdleTimeout(idleTimeout);
        c.setCharset(charset);
        return c;
    }

    public int getSocketRecvBuffer() {
        return socketRecvBuffer;
    }

    public void setSocketRecvBuffer(int socketRecvBuffer) {
        this.socketRecvBuffer = socketRecvBuffer;
    }

    public int getSocketSendBuffer() {
        return socketSendBuffer;
    }

    public void setSocketSendBuffer(int socketSendBuffer) {
        this.socketSendBuffer = socketSendBuffer;
    }

    public int getPacketHeaderSize() {
        return packetHeaderSize;
    }

    public void setPacketHeaderSize(int packetHeaderSize) {
        this.packetHeaderSize = packetHeaderSize;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public int getWriteQueueCapcity() {
        return writeQueueCapcity;
    }

    public void setWriteQueueCapcity(int writeQueueCapcity) {
        this.writeQueueCapcity = writeQueueCapcity;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}
