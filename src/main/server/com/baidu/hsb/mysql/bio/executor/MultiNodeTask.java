/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2014 All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import static com.baidu.hsb.route.RouteResultsetNode.DEFAULT_REPLICA_INDEX;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
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
 * 
 * @author xiongzhao@baidu.com
 * @version $Id: MultiNodeTask.java, v 0.1 2014年9月11日 下午8:19:03 HI:brucest0078 Exp $
 */
public class MultiNodeTask {
    private static final Logger         LOGGER             = Logger
                                                               .getLogger(MultiNodeExecutor.class);
    private static final int            RECEIVE_CHUNK_SIZE = 16 * 1024;

    private AtomicBoolean               isFail             = new AtomicBoolean(false);
    private int                         unfinishedNodeCount;
    private int                         errno;
    private String                      errMessage;
    private AtomicBoolean               fieldEOF           = new AtomicBoolean(false);
    private byte                        packetId;
    private long                        affectedRows;
    private long                        insertId;
    private ByteBuffer                  buffer;
    private final ReentrantLock         lock               = new ReentrantLock();
    private final Condition             taskFinished       = lock.newCondition();
    private final DefaultCommitExecutor icExecutor         = new DefaultCommitExecutor() {
                                                               @Override
                                                               protected String getErrorMessage() {
                                                                   return "Internal commit";
                                                               }

                                                               @Override
                                                               protected Logger getLogger() {
                                                                   return MultiNodeTask.LOGGER;
                                                               }

                                                           };
    private long                        nodeCount          = 0;
    private long                        totalCount         = 0;

    private AtomicLong                  exeTime            = new AtomicLong(0);
    private RouteResultsetNode[]        nodes;
    private boolean                     autocommit;
    private BlockingSession             ss;
    private int                         flag;
    private String                      sql;

    public MultiNodeTask(RouteResultsetNode[] nodes, final boolean autocommit,
                         final BlockingSession ss, final int flag, final String sql) {

        this.nodes = nodes;
        this.autocommit = autocommit;
        this.ss = ss;
        this.sql = sql;
        this.flag = flag;

        this.isFail.set(false);
        this.unfinishedNodeCount = 0;
        this.nodeCount = 0;
        for (RouteResultsetNode rrn : nodes) {
            unfinishedNodeCount += rrn.getSqlCount();
            this.nodeCount++;
        }
        totalCount = unfinishedNodeCount;
        this.errno = 0;
        this.errMessage = null;
        this.fieldEOF.set(false);

        this.packetId = 0;
        this.affectedRows = 0L;
        this.insertId = 0L;
        this.buffer = ss.getSource().allocate();

        if (ss.getSource().isClosed()) {
            decrementCountToZero();
            ss.getSource().recycle(this.buffer);
            return;
        }

        // 多节点处理
        ConcurrentMap<RouteResultsetNode, Channel> target = ss.getTarget();
        for (RouteResultsetNode rrn : nodes) {
            Channel c = target.get(rrn);
            if (c != null) {
                c.setRunning(true);
            }
        }
    }

    /**
     * 是否已完成
     * 
     * @return
     */
    public boolean isTaskFinish() {
        return unfinishedNodeCount <= 0;
    }

    public void terminate() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (unfinishedNodeCount > 0) {
                taskFinished.await();
            }
        } finally {
            lock.unlock();
        }
        icExecutor.terminate();
    }

    private void decrementCountToZero() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            unfinishedNodeCount = 0;
            taskFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean decrementCountAndIsZero(int c) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            unfinishedNodeCount = unfinishedNodeCount - c;
            int ufc = unfinishedNodeCount;
            taskFinished.signalAll();
            return ufc <= 0;
        } finally {
            lock.unlock();
        }
    }

    public void execute() {
        ThreadPoolExecutor exec = ss.getSource().getProcessor().getExecutor();
        for (final RouteResultsetNode rrn : nodes) {
            final Channel c = ss.getTarget().get(rrn);
            if (c != null) {
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        execute0(rrn, c, autocommit, ss, flag, sql, exeTime);
                    }
                });
            } else {
                newExecute(rrn, autocommit, ss, flag, sql, exeTime);
            }
        }
    }

    /**
     * 新通道的执行
     */
    private void newExecute(final RouteResultsetNode rrn, final boolean autocommit,
                            final BlockingSession ss, final int flag, final String sql,
                            final AtomicLong exeTime) {
        final ServerConnection sc = ss.getSource();

        // 检查数据节点是否存在
        HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
        final MySQLDataNode dn = conf.getDataNodes().get(rrn.getName());
        if (dn == null) {
            handleFailure(ss, rrn, new SimpleErrInfo(new UnknownDataNodeException(
                "Unknown dataNode '" + rrn.getName() + "'"), ErrorCode.ER_BAD_DB_ERROR, sc, rrn),
                rrn.getSqlCount(), exeTime, sql);
            return;
        }

        //提交执行任务
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runTask(rrn, dn, ss, sc, autocommit, flag, sql, exeTime);
                } catch (Exception e) {
                    killServerTask(rrn, ss);
                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_MULTI_EXEC_ERROR, sc,
                        rrn), rrn.getSqlCount(), exeTime, sql);
                }
                //                try {
                //
                //                    //5s读取时间，否则超时
                //                    MultiExecutorTask.runTask(new Callable<Boolean>() {
                //
                //                        @Override
                //                        public Boolean call() throws Exception {
                //                            return true;
                //                        }
                //                    }, 5);
                //                } catch (InterruptedException e) {
                //                    killServerTask(rrn, ss);
                //                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_NET_READ_INTERRUPTED,
                //                        sc, rrn), rrn.getSqlCount(), exeTime, sql);
                //                } catch (ExecutionException e) {
                //                    killServerTask(rrn, ss);
                //                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_MULTI_EXEC_ERROR, sc,
                //                        rrn), rrn.getSqlCount(), exeTime, sql);
                //                } catch (TimeoutException e) {
                //                    killServerTask(rrn, ss);
                //                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_MULTI_QUERY_TIMEOUT,
                //                        sc, rrn), rrn.getSqlCount(), exeTime, sql);
                //                }

            }
        });
    }

    private void killServerTask(RouteResultsetNode rrn, BlockingSession ss) {

        ConcurrentMap<RouteResultsetNode, Channel> target = ss.getTarget();
        Channel c = target.get(rrn);
        if (c != null) {

            c.kill();
        }
    }

    private void runTask(final RouteResultsetNode rrn, final MySQLDataNode dn,
                         final BlockingSession ss, final ServerConnection sc,
                         final boolean autocommit, final int flag, final String sql,
                         final AtomicLong exeTime) {
        // 取得数据通道
        int i = rrn.getReplicaIndex();
        Channel c = null;
        try {
            c = (i == DEFAULT_REPLICA_INDEX) ? dn.getMaxUseChannel() : dn.getMaxUseChannel(i);
        } catch (final Exception e) {
            handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_BAD_DB_ERROR, sc, rrn),
                rrn.getSqlCount(), exeTime, sql);
            return;
        }

        c.setRunning(true);
        Channel old = ss.getTarget().put(rrn, c);
        if (old != null && c != old) {
            old.close();
        }

        // 执行
        execute0(rrn, c, autocommit, ss, flag, sql, exeTime);
    }

    /**
     * 执行
     */
    private void execute0(RouteResultsetNode rrn, Channel c, boolean autocommit,
                          BlockingSession ss, int flag, final String sql, final AtomicLong exeTime) {

        ServerConnection sc = ss.getSource();
        if (isFail.get() || sc.isClosed()) {
            c.setRunning(false);
            handleFailure(ss, rrn, null, rrn.getSqlCount(), exeTime, sql);
            return;
        }
        long s = System.currentTimeMillis();

        extSql: for (final String stmt : rrn.getStatement()) {

            try {
                // 执行并等待返回
                BinaryPacket bin = ((MySQLChannel) c).execute(stmt, rrn, sc, autocommit);
                //System.out.println(rrn.getName() + ",sql[" + stmt + "]");
                //LOGGER.info("node[" + rrn.getName()+"],sql["+stmt+"],recv=>"+ByteUtil.formatByte(bin.data)+"<=");
                // 接收和处理数据
                final ReentrantLock lock = MultiNodeTask.this.lock;
                lock.lock();
                try {
                    switch (bin.data[0]) {
                        case ErrorPacket.FIELD_COUNT:
                            c.setRunning(false);
                            handleFailure(ss, rrn,
                                new BinaryErrInfo((MySQLChannel) c, bin, sc, rrn), 1, exeTime, sql);
                            break;
                        case OkPacket.FIELD_COUNT:
                            OkPacket ok = new OkPacket();
                            ok.read(bin);
                            affectedRows += ok.affectedRows;
                            // set lastInsertId
                            if (ok.insertId > 0) {
                                insertId = (insertId == 0) ? ok.insertId : Math.min(insertId,
                                    ok.insertId);
                            }
                            c.setRunning(false);
                            handleSuccessOK(ss, rrn, autocommit, ok);
                            break;
                        default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
                            final MySQLChannel mc = (MySQLChannel) c;
                            if (fieldEOF.get()) {
                                for (;;) {
                                    bin = mc.receive();
                                    //                                    LOGGER.info("FIELD_EOF:"
                                    //                                                + com.baidu.hsb.route.util.ByteUtil
                                    //                                                    .formatByte(bin.data));
                                    switch (bin.data[0]) {
                                        case ErrorPacket.FIELD_COUNT:
                                            c.setRunning(false);
                                            handleFailure(ss, rrn, new BinaryErrInfo(mc, bin, sc,
                                                rrn), 1, exeTime, sql);
                                            continue extSql;
                                        case EOFPacket.FIELD_COUNT:
                                            handleRowData(rrn, c, ss, exeTime, sql);
                                            continue extSql;
                                        default:
                                            continue;
                                    }
                                }
                            } else {
                                bin.packetId = ++packetId;// HEADER
                                List<MySQLPacket> headerList = new LinkedList<MySQLPacket>();
                                headerList.add(bin);
                                for (;;) {
                                    bin = mc.receive();
                                    //LOGGER.info("NO_FIELD_EOF:" + com.baidu.hsb.route.util.ByteUtil.formatByte(bin.data));
                                    switch (bin.data[0]) {
                                        case ErrorPacket.FIELD_COUNT:
                                            c.setRunning(false);
                                            handleFailure(ss, rrn, new BinaryErrInfo(mc, bin, sc,
                                                rrn), 1, exeTime, sql);
                                            continue extSql;
                                        case EOFPacket.FIELD_COUNT:
                                            bin.packetId = ++packetId;// FIELD_EOF
                                            for (MySQLPacket packet : headerList) {
                                                buffer = packet.write(buffer, sc);
                                            }
                                            headerList = null;
                                            buffer = bin.write(buffer, sc);
                                            fieldEOF.set(true);
                                            handleRowData(rrn, c, ss, exeTime, sql);
                                            continue extSql;
                                        default:
                                            bin.packetId = ++packetId;// FIELDS
                                            switch (flag) {
                                                case RouteResultset.REWRITE_FIELD:
                                                    StringBuilder fieldName = new StringBuilder();
                                                    fieldName.append("Tables_in_").append(
                                                        ss.getSource().getSchema());
                                                    FieldPacket field = PacketUtil.getField(bin,
                                                        fieldName.toString());
                                                    headerList.add(field);
                                                    break;
                                                default:
                                                    headerList.add(bin);
                                            }
                                    }
                                }
                            }
                    }
                } finally {
                    lock.unlock();
                    //                    System.out.println("sql[" + stmt + "]suc pkId:" + bin.packetId);
                }
            } catch (final IOException e) {
                c.close();
                handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn), 1, exeTime,
                    sql);
            } catch (final RuntimeException e) {
                c.close();
                handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn), 1, exeTime,
                    sql);
            } finally {
                long e = System.currentTimeMillis() - s;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + rrn.getName() + "][" + stmt + "]" + "exetime:" + e
                                 + "ms pre:" + exeTime.get());
                }
                exeTime.getAndAdd(e);
            }

        }

    }

    /**
     * 处理RowData数据
     */
    private void handleRowData(final RouteResultsetNode rrn, Channel c, BlockingSession ss,
                               final AtomicLong exeTime, final String sql) throws IOException {
        final ServerConnection source = ss.getSource();
        BinaryPacket bin = null;
        int size = 0;
        for (;;) {
            bin = ((MySQLChannel) c).receive();
            //System.out.println(rrn.getName() + "rowData-->");
            switch (bin.data[0]) {
                case ErrorPacket.FIELD_COUNT:
                    c.setRunning(false);
                    handleFailure(ss, rrn, new BinaryErrInfo(((MySQLChannel) c), bin, source, rrn),
                        1, exeTime, sql);
                    return;
                case EOFPacket.FIELD_COUNT:
                    c.setRunning(false);
                    //忽略自动提交
                    if (source.isAutocommit()) {
                        c = ss.getTarget().remove(rrn);
                        if (c != null) {
                            if (isFail.get() || source.isClosed()) {
                                c.close();
                            } else {
                                c.release();
                            }
                        }
                    }
                    handleSuccessEOF(ss, rrn, bin, exeTime, sql);
                    return;
                default:
                    bin.packetId = ++packetId;// ROWS
                    buffer = bin.write(buffer, source);
                    size += bin.packetLength;
                    if (size > RECEIVE_CHUNK_SIZE) {
                        //                        LOGGER.info(rrn.getName() + "hasNext-->");
                        handleNext(rrn, c, ss, exeTime, sql);
                        return;
                    }
            }
        }
    }

    /**
     * 处理下一个任务
     */
    private void handleNext(final RouteResultsetNode rrn, final Channel c,
                            final BlockingSession ss, final AtomicLong exeTime, final String sql) {
        final ServerConnection sc = ss.getSource();
        //        sc.getProcessor().getExecutor().execute(new Runnable() {
        //            @Override
        //            public void run() {
        final ReentrantLock lock = MultiNodeTask.this.lock;
        lock.lock();
        try {
            handleRowData(rrn, c, ss, exeTime, sql);
        } catch (final IOException e) {
            c.close();
            handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn), 1, exeTime, sql);
        } catch (final RuntimeException e) {
            c.close();
            handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn), 1, exeTime, sql);
        } finally {
            lock.unlock();
        }
        //            }
        //        });
    }

    /**
     * @throws nothing
     *             never throws any exception
     */
    private void handleSuccessEOF(BlockingSession ss, final RouteResultsetNode rrn,
                                  BinaryPacket bin, final AtomicLong exeTime, final String sql) {

        if (decrementCountAndIsZero(1)) {
            try {
                if (isFail.get()) {
                    notifyFailure(ss);
                    return;
                }
                try {
                    ServerConnection source = ss.getSource();
                    //忽略自动提交
                    if (source.isAutocommit()) {
                        ss.release();
                    }

                    bin.packetId = ++packetId;// LAST_EOF
                    source.write(bin.write(buffer, source));
                } catch (Exception e) {
                    LOGGER.warn("exception happens in success notification: " + ss.getSource(), e);
                }
            } finally {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("final exeTime-->" + exeTime.get() + ",nodeCount:" + nodeCount);
                }
                LoggerUtil.printDigest(LOGGER, (exeTime.get() / nodeCount), sql);
            }
        }
    }

    /**
     * @throws nothing
     *             never throws any exception
     */
    private void handleSuccessOK(BlockingSession ss, RouteResultsetNode rrn, boolean autocommit,
                                 OkPacket ok) {
        if (decrementCountAndIsZero(1)) {
            if (isFail.get()) {
                notifyFailure(ss);
                return;
            }
            try {
                ServerConnection source = ss.getSource();
                ok.packetId = ++packetId;// OK_PACKET
                ok.affectedRows = affectedRows;
                if (insertId > 0) {
                    ok.insertId = insertId;
                    source.setLastInsertId(insertId);
                }

                if (source.isAutocommit()) {
                    if (!autocommit) { // 前端非事务模式，后端事务模式，则需要自动递交后端事务。
                        icExecutor.commit(ok, ss, ss.getTarget().size());
                    } else {
                        ss.release();
                        ok.write(source);
                    }
                } else {
                    //多节点情况下以非事务模式执行
                    ok.write(source);
                }

                source.recycle(buffer);
            } catch (Exception e) {
                LOGGER.warn("exception happens in success notification: " + ss.getSource(), e);
            }
        }
    }

    private void handleFailure(BlockingSession ss, RouteResultsetNode rrn, ErrInfo errInfo, int c,
                               final AtomicLong exeTime, final String sql) {
        try {
            // 标记为执行失败，并记录第一次异常信息。
            if (!isFail.getAndSet(true) && errInfo != null) {
                errno = errInfo.getErrNo();
                errMessage = errInfo.getErrMsg();
                LOGGER.warn(rrn.getName() + " error[" + errInfo.getErrNo() + ","
                            + errInfo.getErrMsg() + "] in sql[" + sql + "]");
                errInfo.logErr();
            }
        } catch (Exception e) {
            LOGGER.warn("handleFailure failed in " + getClass().getSimpleName() + ", source = "
                        + ss.getSource(), e);
        } finally {
            LoggerUtil
                .printDigest(LOGGER,
                    (long) (exeTime.get() / ((double) (totalCount - unfinishedNodeCount)
                                             * nodeCount / (double) totalCount)), sql);
        }
        if (decrementCountAndIsZero(c)) {
            notifyFailure(ss);
        }
    }

    /**
     * 通知，执行异常
     * 
     * @throws nothing
     *             never throws any exception
     */
    private void notifyFailure(BlockingSession ss) {
        try {
            // 清理
            ss.clear();

            ServerConnection sc = ss.getSource();
            sc.setTxInterrupt();

            // 通知
            ErrorPacket err = new ErrorPacket();
            err.packetId = ++packetId;// ERROR_PACKET
            err.errno = errno;
            err.message = StringUtil.encode(errMessage, sc.getCharset());
            sc.write(err.write(buffer, sc));
        } catch (Exception e) {
            LOGGER.warn("exception happens in failure notification: " + ss.getSource(), e);
        }
    }

    protected static interface ErrInfo {
        int getErrNo();

        String getErrMsg();

        void logErr();
    }

    protected static class BinaryErrInfo implements ErrInfo {
        private String             errMsg;
        private int                errNo;
        private ServerConnection   source;
        private RouteResultsetNode rrn;
        private MySQLChannel       mc;

        public BinaryErrInfo(MySQLChannel mc, BinaryPacket bin, ServerConnection sc,
                             RouteResultsetNode rrn) {
            this.mc = mc;
            this.source = sc;
            this.rrn = rrn;
            ErrorPacket err = new ErrorPacket();
            err.read(bin);
            this.errMsg = (err.message == null) ? null : StringUtil.decode(err.message,
                mc.getCharset());
            this.errNo = err.errno;
        }

        @Override
        public int getErrNo() {
            return errNo;
        }

        @Override
        public String getErrMsg() {
            return errMsg;
        }

        @Override
        public void logErr() {
            try {
                LOGGER.warn(mc.getErrLog(rrn.getLogger(), errMsg, source));
            } catch (Exception e) {
            }
        }
    }

    protected static class SimpleErrInfo implements ErrInfo {
        private Exception          e;
        private int                errNo;
        private ServerConnection   source;
        private RouteResultsetNode rrn;

        public SimpleErrInfo(Exception e, int errNo, ServerConnection sc, RouteResultsetNode rrn) {
            this.e = e;
            this.errNo = errNo;
            this.source = sc;
            this.rrn = rrn;
        }

        @Override
        public int getErrNo() {
            return errNo;
        }

        @Override
        public String getErrMsg() {
            String msg = e.getMessage();
            return msg == null ? e.getClass().getSimpleName() : msg;
        }

        @Override
        public void logErr() {
            try {
                LOGGER.warn(new StringBuilder().append(source).append(rrn).toString(), e);
            } catch (Exception e) {
            }
        }
    }

}
