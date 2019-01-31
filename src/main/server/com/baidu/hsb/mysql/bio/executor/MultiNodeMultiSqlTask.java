/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import static com.baidu.hsb.route.RouteResultsetNode.DEFAULT_REPLICA_INDEX;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.baidu.hsb.HeisenbergConfig;
import com.baidu.hsb.HeisenbergServer;
import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.util.ByteUtil;
import com.baidu.hsb.config.util.LoggerUtil;
import com.baidu.hsb.exception.UnknownDataNodeException;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.mysql.bio.Channel;
import com.baidu.hsb.mysql.bio.MySQLChannel;
import com.baidu.hsb.mysql.bio.executor.MultiNodeTask.ErrInfo;
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
import com.baidu.hsb.server.session.FailCondCallback;
import com.baidu.hsb.server.session.SucCondCallback;

/**
 * 多节点多sql任务器
 * 
 * @author brucexx
 *
 */
public class MultiNodeMultiSqlTask extends MultiNodeTask {

    private SucCondCallback sc = null;
    private FailCondCallback fc = null;

    private String[] m_sql;

    /**
     * @param nodes
     * @param autocommit
     * @param ss
     * @param flag
     * @param sql
     * @param type
     */
    public MultiNodeMultiSqlTask(RouteResultsetNode[] nodes, boolean autocommit, BlockingSession ss, int flag,
            String[] sql, int type, SucCondCallback sc, FailCondCallback fc) {
        super(nodes, autocommit, ss, flag, null, type);
        this.m_sql = sql;
        this.sc = sc;
        this.fc = fc;
    }

    public void execute() {
        ThreadPoolExecutor exec = ss.getSource().getProcessor().getExecutor();
        for (final RouteResultsetNode rrn : nodes) {
            final Channel c = ss.getTarget().get(rrn);
            if (c != null) {
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        execute0(rrn, c, autocommit, ss, flag, m_sql, exeTime);
                    }
                });
            } else {
                newExecute(rrn, autocommit, ss, flag, m_sql, exeTime);
            }
        }
    }

    /**
     * 新通道的执行
     */
    protected void newExecute(final RouteResultsetNode rrn, final boolean autocommit, final BlockingSession ss,
            final int flag, final String[] sql, final AtomicLong exeTime) {
        final ServerConnection sc = ss.getSource();

        // 检查数据节点是否存在
        HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
        final MySQLDataNode dn = conf.getDataNodes().get(rrn.getName());
        if (dn == null) {
            handleFailure(ss, rrn,
                    new SimpleErrInfo(new UnknownDataNodeException("Unknown dataNode '" + rrn.getName() + "'"),
                            ErrorCode.ER_BAD_DB_ERROR, sc, rrn),
                    rrn.getSqlCount(), exeTime, sql);
            return;
        }

        // 提交执行任务
        sc.getProcessor().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runTask(rrn, dn, ss, sc, autocommit, flag, sql, exeTime);
                } catch (Exception e) {
                    killServerTask(rrn, ss);
                    handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_MULTI_EXEC_ERROR, sc, rrn),
                            rrn.getSqlCount(), exeTime, sql);
                }

            }
        });
    }

    /**
     * 执行
     */
    protected void execute0(RouteResultsetNode rrn, Channel c, boolean autocommit, BlockingSession ss, int flag,
            final String[] sql, final AtomicLong exeTime) {

        ServerConnection sc = ss.getSource();
        if (isFail.get() || sc.isClosed()) {
            c.setRunning(false);
            handleFailure(ss, rrn, null, rrn.getSqlCount(), exeTime, sql);
            return;
        }
        long s = System.currentTimeMillis();

        extSql: for (final String stmt : sql) {

            try {
                // 执行并等待返回
                BinaryPacket bin = ((MySQLChannel) c).execute(stmt, rrn, sc, autocommit);
                // System.out.println(rrn.getName() + ",sql[" + stmt + "]");
                // LOGGER.info("node[" + rrn.getName()+"],sql["+stmt+"],recv=>"+ByteUtil.formatByte(bin.data)+"<=");
                // 接收和处理数据
                final ReentrantLock lock = MultiNodeMultiSqlTask.this.lock;
                lock.lock();
                try {
                    switch (bin.data[0]) {
                        case ErrorPacket.FIELD_COUNT:
                            c.setRunning(false);
                            handleFailure(ss, rrn, new BinaryErrInfo((MySQLChannel) c, bin, sc, rrn), 1, exeTime, sql);
                            break;
                        case OkPacket.FIELD_COUNT:
                            OkPacket ok = new OkPacket();
                            ok.read(bin);
                            affectedRows += ok.affectedRows;
                            // set lastInsertId
                            if (ok.insertId > 0) {
                                insertId = (insertId == 0) ? ok.insertId : Math.min(insertId, ok.insertId);
                            }
                            if (runData.get(rrn.getName()).decrementAndGet() == 0) {
                                c.setRunning(false);
                            }
                            handleSuccessOK(ss, rrn, autocommit, ok);
                            break;
                        default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
                            final MySQLChannel mc = (MySQLChannel) c;
                            if (fieldEOF.get()) {
                                for (;;) {
                                    // System.out.println(taskName+" eof recv++");
                                    bin = mc.receive();
                                    switch (bin.data[0]) {
                                        case ErrorPacket.FIELD_COUNT:
                                            c.setRunning(false);
                                            handleFailure(ss, rrn, new BinaryErrInfo(mc, bin, sc, rrn), 1, exeTime,
                                                    sql);
                                            continue extSql;
                                        case EOFPacket.FIELD_COUNT:
                                            // System.out.println(taskName+" eof data++");
                                            handleRowData(rrn, c, ss, exeTime, sql);
                                            continue extSql;
                                        default:
                                            // 直接过滤掉fields
                                            continue;
                                    }
                                }
                            } else {
                                bin.packetId = ++packetId;// HEADER
                                List<MySQLPacket> headerList = new LinkedList<MySQLPacket>();
                                headerList.add(bin);
                                for (;;) {
                                    bin = mc.receive();
                                    // LOGGER.info("NO_FIELD_EOF:" +
                                    // com.baidu.hsb.route.util.ByteUtil.formatByte(bin.data));
                                    switch (bin.data[0]) {
                                        case ErrorPacket.FIELD_COUNT:
                                            c.setRunning(false);
                                            handleFailure(ss, rrn, new BinaryErrInfo(mc, bin, sc, rrn), 1, exeTime,
                                                    sql);
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
                    }
                } finally {
                    lock.unlock();
                    // System.out.println("sql[" + stmt + "]suc pkId:" + bin.packetId);
                }
            } catch (final IOException e) {
                c.close();
                handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn), 1, exeTime, sql);
            } catch (final RuntimeException e) {
                c.close();
                handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn), 1, exeTime, sql);
            } finally {
                long e = System.currentTimeMillis() - s;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + rrn.getName() + "][" + stmt + "]" + "exetime:" + e + "ms pre:" + exeTime.get());
                }
                exeTime.getAndAdd(e);
            }

        }

    }

    /**
     * @throws nothing never throws any exception
     */
    protected void handleSuccessOK(BlockingSession ss, RouteResultsetNode rrn, boolean autocommit, OkPacket ok) {
        if (decrementCountAndIsZero(1)) {
            if (isFail.get()) {
                notifyFailure(ss);
                return;
            }
            if (this.sc != null) {
                // 这里用于联合执行完的结果，主要是redis保存分片，如果出错,将给上层返回，用于rollback
                try {
                    this.sc.condition();
                } catch (Exception e) {
                    LOGGER.error(rrn.getStatement() + "保存分片出错", e);
                    //
                    errno = ErrorCode.ER_XA_SAVE_REDIS_ERROR;
                    notifyFailure(ss);
                    // handleFailure(ErrorCode.ER_XA_SAVE_REDIS_ERROR, "保存xa状态出错", ss);
                }
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
                        // 写入事件
                        source.writeCode(true, 0);
                    }
                } else {
                    // 多节点情况下以非事务模式执行
                    ok.write(source);
                    // 写入事件
                    source.writeCode(true, 0);
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
                LOGGER.warn(rrn.getName() + " error[" + errInfo.getErrNo() + "," + errInfo.getErrMsg() + "] in sql["
                        + sql + "]");
                errInfo.logErr();
            }
        } catch (Exception e) {
            LOGGER.warn("handleFailure failed in " + getClass().getSimpleName() + ", source = " + ss.getSource(), e);
        } finally {
            LoggerUtil
                    .printDigest(LOGGER,
                            (long) (exeTime.get()
                                    / ((double) (totalCount - unfinishedNodeCount) * nodeCount / (double) totalCount)),
                            sql);
        }
        if (decrementCountAndIsZero(c)) {
            notifyFailure(ss);
        }
    }

    protected void handleSuccessEOF(BlockingSession ss, final RouteResultsetNode rrn, BinaryPacket bin,
            final AtomicLong exeTime, final String[] sql) {

        if (decrementCountAndIsZero(1)) {
            try {
                if (isFail.get()) {
                    notifyFailure(ss);
                    return;
                }
                try {
                    ServerConnection source = ss.getSource();
                    // 忽略自动提交
                    if (source.isAutocommit()) {
                        ss.release();
                    }
                    if (flag == RouteResultset.SUM_FLAG || flag == RouteResultset.MAX_FLAG
                            || flag == RouteResultset.MIN_FLAG) {
                        BinaryPacket data = new BinaryPacket();
                        data.packetId = ++packetId;
                        data.data = funcCachedData;
                        buffer = data.write(buffer, source);
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

                LoggerUtil.printDigest(LOGGER, (exeTime.get() / nodeCount),
                        com.baidu.hsb.route.util.StringUtil.join(sql, '#'));
            }
        }
    }

    protected void handleRowData(final RouteResultsetNode rrn, Channel c, BlockingSession ss, final AtomicLong exeTime,
            final String[] sql) throws IOException {
        final ServerConnection source = ss.getSource();
        BinaryPacket bin = null;
        // int size = 0;
        for (;;) {
            bin = ((MySQLChannel) c).receive();
            // System.out.println(rrn.getName() + "rowData-->");
            switch (bin.data[0]) {
                case ErrorPacket.FIELD_COUNT:
                    c.setRunning(false);
                    handleFailure(ss, rrn, new BinaryErrInfo(((MySQLChannel) c), bin, source, rrn), 1, exeTime, sql);
                    return;
                case EOFPacket.FIELD_COUNT:
                    if (runData.get(rrn.getName()).decrementAndGet() == 0) {
                        c.setRunning(false);
                    }
                    // 忽略自动提交
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
                    if (flag == RouteResultset.SUM_FLAG || flag == RouteResultset.MAX_FLAG
                            || flag == RouteResultset.MIN_FLAG) {
                        funcCachedData = ByteUtil.calc(funcCachedData, bin.getData(), flag);
                    } else {
                        bin.packetId = ++packetId;// ROWS
                        buffer = bin.write(buffer, source);
                        // size += bin.packetLength;
                        // if (size > RECEIVE_CHUNK_SIZE) {
                        // // LOGGER.info(rrn.getName() + "hasNext-->");
                        // handleNext(rrn, c, ss, exeTime, sql);
                        // return;
                        // }
                    }

            }
        }
    }

    protected void runTask(final RouteResultsetNode rrn, final MySQLDataNode dn, final BlockingSession ss,
            final ServerConnection sc, final boolean autocommit, final int flag, final String[] sql,
            final AtomicLong exeTime) {
        // 取得数据通道
        int i = rrn.getReplicaIndex();
        Channel c = null;
        try {
            c = (i == DEFAULT_REPLICA_INDEX) ? dn.getChannel() : dn.getChannel(i);
        } catch (final Exception e) {
            handleFailure(ss, rrn, new SimpleErrInfo(e, ErrorCode.ER_BAD_DB_ERROR, sc, rrn), rrn.getSqlCount(), exeTime,
                    sql);
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

    protected void handleFailure(BlockingSession ss, RouteResultsetNode rrn, ErrInfo errInfo, int c,
            final AtomicLong exeTime, final String[] sql) {
        try {
            // 标记为执行失败，并记录第一次异常信息。
            if (!isFail.getAndSet(true) && errInfo != null) {
                errno = errInfo.getErrNo();
                errMessage = errInfo.getErrMsg();
                LOGGER.warn(rrn.getName() + " error[" + errInfo.getErrNo() + "," + errInfo.getErrMsg() + "] in sql["
                        + sql + "]");
                errInfo.logErr();
            }
        } catch (Exception e) {
            LOGGER.warn("handleFailure failed in " + getClass().getSimpleName() + ", source = " + ss.getSource(), e);
        } finally {
            String sSql = com.baidu.hsb.route.util.StringUtil.join(sql, '#');
            LoggerUtil
                    .printDigest(LOGGER,
                            (long) (exeTime.get()
                                    / ((double) (totalCount - unfinishedNodeCount) * nodeCount / (double) totalCount)),
                            sSql);
        }
        if (decrementCountAndIsZero(c)) {
            notifyFailure(ss);
        }
    }

}
