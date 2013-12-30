/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.net.factory;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.baidu.hsb.net.BackendConnection;
import com.baidu.hsb.net.NIOConnector;
import com.baidu.hsb.net.buffer.BufferQueue;

/**
 * 
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: BackendConnectionFactory.java, v 0.1 2013年12月26日 下午6:01:52 HI:brucest0078 Exp $
 */
public abstract class BackendConnectionFactory {

    protected int socketRecvBuffer = 16 * 1024;
    protected int socketSendBuffer = 8 * 1024;
    protected int packetHeaderSize = 4;
    protected int maxPacketSize = 16 * 1024 * 1024;
    protected int writeQueueCapcity = 8;
    protected long idleTimeout = 8 * 3600 * 1000L;

    protected SocketChannel openSocketChannel() throws IOException {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            Socket socket = channel.socket();
            socket.setReceiveBufferSize(socketRecvBuffer);
            socket.setSendBufferSize(socketSendBuffer);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
        } catch (IOException e) {
            closeChannel(channel);
            throw e;
        } catch (RuntimeException e) {
            closeChannel(channel);
            throw e;
        }
        return channel;
    }

    protected void postConnect(BackendConnection c, NIOConnector connector) {
        c.setPacketHeaderSize(packetHeaderSize);
        c.setMaxPacketSize(maxPacketSize);
        c.setWriteQueue(new BufferQueue(writeQueueCapcity));
        c.setIdleTimeout(idleTimeout);
        c.setConnector(connector);
        connector.postConnect(c);
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

    private static void closeChannel(SocketChannel channel) {
        if (channel == null) {
            return;
        }
        Socket socket = channel.socket();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        try {
            channel.close();
        } catch (IOException e) {
        }
    }

}
