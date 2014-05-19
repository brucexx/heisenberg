/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import static com.baidu.hsb.route.RouteResultsetNode.DEFAULT_REPLICA_INDEX;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.util.LoggerUtil;
import com.baidu.hsb.exception.UnknownDataNodeException;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.mysql.bio.Channel;
import com.baidu.hsb.mysql.bio.MySQLChannel;
import com.baidu.hsb.net.mysql.BinaryPacket;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.route.RouteResultset;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.session.BlockingSession;
import com.baidu.hsb.util.StringUtil;

/**
 * 单节点数据执行器
 * 
 * @author xiongzhao@baidu.com
 */
public final class SingleNodeExecutor extends NodeExecutor {

    private static final Logger LOGGER             = Logger.getLogger(SingleNodeExecutor.class);
    private static final int    RECEIVE_CHUNK_SIZE = 64 * 1024;

    private byte                packetId;
    private boolean             isRunning          = false;
    private final ReentrantLock lock               = new ReentrantLock();
    private final Condition     taskFinished       = lock.newCondition();

    @Override
    public void terminate() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (isRunning) {
                taskFinished.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 单数据节点执行
     */
    public void execute(RouteResultsetNode rrn, BlockingSession ss, int flag, final String sql) {
        // 初始化
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.packetId = 0;
            this.isRunning = true;
        } finally {
            lock.unlock();
        }

        // 检查连接是否已关闭
        if (ss.getSource().isClosed()) {
            endRunning();
            return;
        }
        final AtomicLong exeTime = new AtomicLong(0);
        // 单节点处理
        Channel c = ss.getTarget().get(rrn);
        if (c != null) {
            c.setRunning(true);
            bindingExecute(rrn, ss, c, flag, sql, exeTime);
        } else {
            newExecute(rrn, ss, flag, sql, exeTime);
        }
    }

    /**
     * 已绑定数据通道的执行
     */
    private void bindingExecute(final RouteResultsetNode rrn, final BlockingSession ss,
                                final Channel c, final int flag, final String sql,
                                final AtomicLong exeTime) {
        ss.getSource().getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                execute0(rrn, ss, c, flag, sql, exeTime);
            }
        });
    }

    /**
     * 新数据通道的执行
     */
    private void newExecute(final RouteResultsetNode rrn, final BlockingSession ss, final int flag,
                            final String sql, final AtomicLong exeTime) {

        final ServerConnection sc = ss.getSource();

        // 检查数据节点是否存在
        HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
        final MySQLDataNode dn = conf.getDataNodes().get(rrn.getName());
        if (dn == null) {
            LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(),
                new UnknownDataNodeException());
            handleError(ErrorCode.ER_BAD_DB_ERROR, "Unknown dataNode '" + rrn.getName() + "'", ss);
            return;
        }

        // 提交执行任务
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // 取得数据通道
                int i = rrn.getReplicaIndex();
                Channel c = null;
                try {
                    c = (i == DEFAULT_REPLICA_INDEX) ? dn.getChannel() : dn.getChannel(i);
                } catch (Exception e) {
                    LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
                    String msg = e.getMessage();
                    handleError(ErrorCode.ER_BAD_DB_ERROR, msg == null ? e.getClass()
                        .getSimpleName() : msg, ss);
                    return;
                }

                // 检查连接是否已关闭。
                if (sc.isClosed()) {
                    c.release();
                    endRunning();
                    return;
                }

                // 绑定数据通道
                c.setRunning(true);
                Channel old = ss.getTarget().put(rrn, c);
                if (old != null && old != c) {
                    old.close();
                }

                // 执行
                execute0(rrn, ss, c, flag, sql, exeTime);
            }
        });
    }

    /**
     * 数据通道执行
     */
    private void execute0(RouteResultsetNode rrn, BlockingSession ss, Channel c, int flag,
                          final String sql, final AtomicLong exeTime) {
        final ServerConnection sc = ss.getSource();
        long s = System.currentTimeMillis();

        // 检查连接是否已关闭
        if (sc.isClosed()) {
            c.setRunning(false);
            endRunning();
            ss.clear();
            return;
        }
        // 执行并等待返回
        MySQLChannel mc = (MySQLChannel) c;
        try {
            for (String stmt : rrn.getStatement()) {
                try {
                    BinaryPacket bin = mc.execute(stmt, rrn, sc, sc.isAutocommit());

                    // 接收和处理数据
                    switch (bin.data[0]) {
                        case OkPacket.FIELD_COUNT: {
                            mc.setRunning(false);
                            if (mc.isAutocommit()) {
                                ss.clear();
                            }
                            endRunning();
                            bin.packetId = ++packetId;// OK_PACKET
                            // set lastInsertId
                            setLastInsertId(bin, sc);
                            sc.write(bin.write(sc.allocate(), sc));
                            break;
                        }
                        case ErrorPacket.FIELD_COUNT: {
                            LOGGER.warn(mc.getErrLog(rrn.getLogger(), mc.getErrMessage(bin), sc));
                            mc.setRunning(false);
                            if (mc.isAutocommit()) {
                                ss.clear();
                            }
                            endRunning();
                            bin.packetId = ++packetId;// ERROR_PACKET
                            sc.write(bin.write(sc.allocate(), sc));
                            break;
                        }
                        default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
                            handleResultSet(rrn, ss, mc, bin, flag);
                    }
                } catch (IOException e) {
                    LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
                    c.close();
                    String msg = e.getMessage();
                    handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg,
                        ss);
                } catch (RuntimeException e) {
                    LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
                    c.close();
                    String msg = e.getMessage();
                    handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg,
                        ss);
                } finally {
                    long e = System.currentTimeMillis();
                    //                    if (LOGGER.isDebugEnabled()) {
                    //                        LOGGER.debug("[" + stmt + "]starttime:" + s + ",endtime:" + e + "");
                    //                    }
                    exeTime.getAndAdd(e - s);
                }
            }
        } finally {
            LoggerUtil.printDigest(LOGGER, exeTime.get(), s, sql);
        }

    }

    /**
     * 处理结果集数据
     */
    private void handleResultSet(RouteResultsetNode rrn, BlockingSession ss, MySQLChannel mc,
                                 BinaryPacket bin, int flag) throws IOException {
        final ServerConnection sc = ss.getSource();

        bin.packetId = ++packetId;// HEADER
        List<MySQLPacket> headerList = new LinkedList<MySQLPacket>();
        headerList.add(bin);
        for (;;) {
            bin = mc.receive();
            switch (bin.data[0]) {
                case ErrorPacket.FIELD_COUNT: {
                    LOGGER.warn(mc.getErrLog(rrn.getLogger(), mc.getErrMessage(bin), sc));
                    mc.setRunning(false);
                    if (mc.isAutocommit()) {
                        ss.clear();
                    }
                    endRunning();
                    bin.packetId = ++packetId;// ERROR_PACKET
                    sc.write(bin.write(sc.allocate(), sc));
                    return;
                }
                case EOFPacket.FIELD_COUNT: {
                    bin.packetId = ++packetId;// FIELD_EOF
                    ByteBuffer bb = sc.allocate();
                    for (MySQLPacket packet : headerList) {
                        bb = packet.write(bb, sc);
                    }
                    bb = bin.write(bb, sc);
                    headerList = null;
                    handleRowData(rrn, ss, mc, bb, packetId);
                    return;
                }
                default:
                    bin.packetId = ++packetId;// FIELDS
                    switch (flag) {
                        case RouteResultset.REWRITE_FIELD:
                            StringBuilder fieldName = new StringBuilder();
                            fieldName.append("Tables_in_").append(ss.getSource().getSchema());
                            FieldPacket field = PacketUtil.getField(bin, fieldName.toString());
                            headerList.add(field);
                            break;
                        default:
                            headerList.add(bin);
                    }
            }
        }
    }

    /**
     * 处理RowData数据
     */
    private void handleRowData(RouteResultsetNode rrn, BlockingSession ss, MySQLChannel mc,
                               ByteBuffer bb, byte id) throws IOException {
        final ServerConnection sc = ss.getSource();
        this.packetId = id;
        BinaryPacket bin = null;
        int size = 0;
        try {
            for (;;) {
                bin = mc.receive();
                switch (bin.data[0]) {
                    case ErrorPacket.FIELD_COUNT:
                        LOGGER.warn(mc.getErrLog(rrn.getLogger(), mc.getErrMessage(bin), sc));
                        mc.setRunning(false);
                        if (mc.isAutocommit()) {
                            ss.clear();
                        }
                        endRunning();
                        bin.packetId = ++packetId;// ERROR_PACKET
                        bb = bin.write(bb, sc);
                        sc.write(bb);
                        return;
                    case EOFPacket.FIELD_COUNT:
                        mc.setRunning(false);
                        if (mc.isAutocommit()) {
                            ss.clear();
                        }
                        endRunning();
                        bin.packetId = ++packetId;// LAST_EOF
                        bb = bin.write(bb, sc);
                        sc.write(bb);
                        return;
                    default:
                        bin.packetId = ++packetId;// ROWS
                        bb = bin.write(bb, sc);
                        size += bin.packetLength;
                        if (size > RECEIVE_CHUNK_SIZE) {
                            handleNext(rrn, ss, mc, bb, packetId);
                            return;
                        }
                }
            }
        } catch (IOException e) {
            sc.recycle(bb);
            throw e;
        }
    }

    /**
     * 下一个数据接收任务
     */
    private void handleNext(final RouteResultsetNode rrn, final BlockingSession ss,
                            final MySQLChannel mc, final ByteBuffer bb, final byte id) {
        final ServerConnection sc = ss.getSource();
        //        sc.getProcessor().getExecutor().execute(new Runnable() {
        //            @Override
        //            public void run() {
        try {
            handleRowData(rrn, ss, mc, bb, id);
        } catch (IOException e) {
            LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
            mc.close();
            String msg = e.getMessage();
            handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
        } catch (RuntimeException e) {
            LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), e);
            mc.close();
            String msg = e.getMessage();
            handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
        }
        //            }
        //        });
    }

    /**
     * 执行异常处理
     */
    private void handleError(int errno, String message, BlockingSession ss) {
        endRunning();

        // 清理
        ss.clear();

        ServerConnection sc = ss.getSource();
        sc.setTxInterrupt();

        // 通知
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;// ERROR_PACKET
        err.errno = errno;
        err.message = StringUtil.encode(message, sc.getCharset());
        err.write(sc);
    }

    private void endRunning() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            isRunning = false;
            taskFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void setLastInsertId(BinaryPacket bin, ServerConnection sc) {
        OkPacket ok = new OkPacket();
        ok.read(bin);
        if (ok.insertId > 0) {
            sc.setLastInsertId(ok.insertId);
        }
    }

}
