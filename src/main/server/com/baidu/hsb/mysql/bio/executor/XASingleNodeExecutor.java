/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import static com.baidu.hsb.route.RouteResultsetNode.DEFAULT_REPLICA_INDEX;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
import com.baidu.hsb.mysql.xa.XAOp;
import com.baidu.hsb.net.mysql.BinaryPacket;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.route.RouteResultset;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.route.util.StringUtil;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.session.BlockingSession;
import com.baidu.hsb.server.session.XASession;

/**
 * 一个连接多个执行，必须要保证这N个操作的类型一致
 * 
 * @author brucexx
 *
 */
public class XASingleNodeExecutor extends SingleNodeExecutor {

	protected static Logger LOGGER = Logger.getLogger(XASingleNodeExecutor.class);

	private boolean IS_DEBUG = true;

	public XASingleNodeExecutor() {
	}

	/**
	 * 单数据节点执行
	 */
	public void execute(RouteResultsetNode rrn, BlockingSession ss, int flag, String oSql, int op) {
		//
		String xId = ss.getSource().getXaSession().getXid();
		LOGGER.info("xId:" + xId + "," + (XAUtil.convert(op)));

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
		String[] sql = XAUtil.gen(op, xId, oSql);
		// op =1必须要复用op=0的连接，这里可能需要后面优化，op>1的操作后面随便使用

		if (c != null && !c.isClosed()) {
			if (op == 1) {
				bindingExecute(rrn, ss, c, flag, sql, exeTime, op);
			} else {
				if (!c.isRunning()) {
					bindingExecute(rrn, ss, c, flag, sql, exeTime, op);
				} else {
					if (IS_DEBUG) {
						//
						LOGGER.info("op[" + op + "]新建之0。。。");
					}
					newExecute(rrn, ss, flag, sql, exeTime, op);
				}
			}

		} else {
			if (IS_DEBUG) {
				//
				LOGGER.info("op[" + op + "]新建之1。。。");
			}
			newExecute(rrn, ss, flag, sql, exeTime, op);
		}
	}

	/**
	 * 新数据通道的执行
	 */
	protected void newExecute(final RouteResultsetNode rrn, final BlockingSession ss, final int flag,
			final String[] sql, final AtomicLong exeTime, int op) {

		final ServerConnection sc = ss.getSource();

		// 检查数据节点是否存在
		HeisenbergConfig conf = HeisenbergServer.getInstance().getConfig();
		final MySQLDataNode dn = conf.getDataNodes().get(rrn.getName());
		if (dn == null) {
			LOGGER.warn(new StringBuilder().append(sc).append(rrn).toString(), new UnknownDataNodeException());
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
					LOGGER.error(new StringBuilder().append(sc).append(rrn).toString(), e);
					String msg = e.getMessage();
					handleError(ErrorCode.ER_BAD_DB_ERROR, msg == null ? e.getClass().getSimpleName() : msg, ss);
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
				execute0(rrn, ss, c, flag, sql, exeTime, op);
			}
		});
	}

	/**
	 * 已绑定数据通道的执行
	 */
	public void bindingExecute(final RouteResultsetNode rrn, final BlockingSession ss, final Channel c, final int flag,
			final String[] sql, final AtomicLong exeTime, final int op) {
		if (IS_DEBUG) {
			//
			LOGGER.info("op[" + op + "]复用之。。。");
		}
		ss.getSource().getProcessor().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				execute0(rrn, ss, c, flag, sql, exeTime, op);
			}
		});
	}

	/**
	 * 数据通道执行
	 */
	public void execute0(RouteResultsetNode rrn, BlockingSession ss, Channel c, int flag, final String[] sql,
			final AtomicLong exeTime, final int op) {
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
			// 这里收集多个成功因子
			AtomicInteger ai = new AtomicInteger(sql.length);
			for (String stmt : sql) {
				// op==0时 先buf conn
				ss.getSource().getXaSession().bufConn(c, stmt, flag);
				// 这里再reuse op==1 必须要命中op==0执行过的连接，如果不是之前执行过的连接选择跳过
				if (op == 1) {
					if (!ss.getSource().getXaSession().xaMustReuse(c, stmt, flag)) {
						ai.decrementAndGet();
						continue;
					}
				}
				if (!ss.getSource().getXaSession().canRepeat(c, stmt)) {
					ai.decrementAndGet();
					continue;
				}
				try {
					BinaryPacket bin = mc.execute(stmt, rrn, sc, false);

					// 接收和处理数据
					switch (bin.data[0]) {
					case OkPacket.FIELD_COUNT: {
						handleSuc(rrn, ss, mc, bin, sc, ai, stmt, flag, op);
						break;
					}
					case ErrorPacket.FIELD_COUNT: {
						// bin.packetId = ++packetId;// ERROR_PACKET
						handleFail(rrn, ss, mc, bin, sc, ai, stmt, flag, op);
						break;
					}
					default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
						handleResultSet(rrn, ss, mc, bin, flag, ai, stmt, op);
					}
				} catch (IOException e) {
					LOGGER.error(new StringBuilder().append(sc).append(rrn).toString(), e);
					c.close();
					String msg = e.getMessage();
					handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
				} catch (RuntimeException e) {
					LOGGER.error(new StringBuilder().append(sc).append(rrn).toString(), e);
					c.close();
					String msg = e.getMessage();
					handleError(ErrorCode.ER_YES, msg == null ? e.getClass().getSimpleName() : msg, ss);
				} finally {
					long e = System.currentTimeMillis();
					// if (LOGGER.isDebugEnabled()) {
					// LOGGER.debug("[" + stmt + "]starttime:" + s + ",endtime:" + e + "");
					// }
					exeTime.getAndAdd(e - s);
				}
			}
		} finally {
			String sSql = StringUtil.join(sql, '#');
			LoggerUtil.printDigest(LOGGER, exeTime.get(), s, sSql);
		}

	}

	private ErrorPacket g(BinaryPacket bin) {
		ErrorPacket err = new ErrorPacket();
		err.read(bin);
		err.packetId = ++this.packetId;
		return err;
	}

	protected boolean sucNeedAck(String stmt, int op) {
		// xa start 只有执行原生的时候才需要ack 或 rollback
		if (!XAUtil.isXaStart(stmt) && (op == 0 || op == 2 || op == 3)) {
			return true;
		}
		return false;
	}

	private boolean failNeedAck(String stmt, ErrorPacket err, MySQLChannel c, int op) {
		if (IS_DEBUG) {
			LOGGER.info(
					"err:" + err.errno + ",msg:" + com.baidu.hsb.util.StringUtil.decode(err.message, c.getCharset()));
		}
		if (!XAUtil.isXAOp(stmt)) {
			return true;
		}
		// rollback失败必须要返回 //过程commit中有失败呢？直接让上层超时吧
		if (op == 3 || op == 1) {
			return true;
		}

		if (XAUtil.isXAOp(stmt) && XAUtil.isXAIgnore(err.errno)) {
			return false;
		}

		return true;
	}

	/**
	 * 处理结果集数据
	 */
	protected void handleResultSet(RouteResultsetNode rrn, BlockingSession ss, MySQLChannel mc, BinaryPacket bin,
			int flag, AtomicInteger ai, String stmt, int op) throws IOException {
		final ServerConnection sc = ss.getSource();

		bin.packetId = ++packetId;// HEADER
		List<MySQLPacket> headerList = new LinkedList<MySQLPacket>();
		headerList.add(bin);
		for (;;) {
			bin = mc.receive();
			switch (bin.data[0]) {
			case ErrorPacket.FIELD_COUNT: {
				handleFail(rrn, ss, mc, bin, sc, ai, stmt, flag, op);
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
				handleRowData(rrn, ss, mc, bb, packetId, ai, stmt, flag, op);
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
	protected void handleRowData(RouteResultsetNode rrn, BlockingSession ss, MySQLChannel mc, ByteBuffer bb, byte id,
			AtomicInteger ai, String stmt, int flag, int op) throws IOException {
		final ServerConnection sc = ss.getSource();
		this.packetId = id;
		BinaryPacket bin = null;
		// int size = 0;
		try {
			for (;;) {
				bin = mc.receive();
				switch (bin.data[0]) {
				case ErrorPacket.FIELD_COUNT:
					handleFail(rrn, ss, mc, bin, sc, ai, stmt, flag, op);
					return;
				case EOFPacket.FIELD_COUNT:
					// last eof
					handleSuc(rrn, ss, mc, bin, sc, ai, stmt, flag, op);
					return;
				default:
					bin.packetId = ++packetId;// ROWS
					bb = bin.write(bb, sc);
					// size += bin.packetLength;
					// if (size > RECEIVE_CHUNK_SIZE) {
					// handleNext(rrn, ss, mc, bb, packetId);
					// return;
					// }
				}
			}
		} catch (IOException e) {
			sc.recycle(bb);
			throw e;
		}
	}

	protected void handleFail(RouteResultsetNode rrn, BlockingSession ss, MySQLChannel mc, BinaryPacket bin,
			ServerConnection sc, AtomicInteger ai, String stmt, int flag, int op) {
		ErrorPacket err = g(bin);
		LOGGER.warn(
				mc.getErrLog(rrn.getLogger(), com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()), sc)
						+ "[" + stmt + "]node;" + rrn.getName());
		// 此处后台做一些额外操作
		XASession xaSession = ss.getSource().getXaSession();
		if (xaSession == null) {
			this.handleError(ErrorCode.ER_XA_CONTEXT_MISSING, "xa session lost", ss);
			return;
		}
		if (op == 2) {
			LOGGER.info("xid:" + xaSession.getXid() + " xa commit error :" + err.errno + ",msg:"
					+ com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()));
			xaSession.release();
		}
		// 回写错误码
		if (!failNeedAck(stmt, err, mc, op)) {
			return;
		}

		// 此处做一些额外操作
		switch (op) {
		// sql错误,给客户端回滚
		case 0:
			LOGGER.info("xid:" + xaSession.getXid() + " xa start error:" + err.errno + ",msg:"
					+ com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()));
			// 回写原错误，上层有回滚，下面也有回滚
			sc.write(err.write(sc.allocate(), sc));
			// 此处不能结束mysqlchannel
			endRunning();
			return;
		// end&prepare 错误 客户端发起回滚，如果客户端回滚不成异步recover(这里可能不需要)
		case 1:
			LOGGER.info("xid:" + xaSession.getXid() + " xa end&prepare error:" + err.errno + ",msg:"
					+ com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()));
			// 回写原错误，上层有回滚，下面也有回滚
			sc.write(err.write(sc.allocate(), sc));
			break;
		// 内部 commit error，不需要管了，异步recover
//		case 2:
//			LOGGER.info("xid:" + xaSession.getXid() + " xa commit error :" + err.errno + ",msg:"
//					+ com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()));
//			// sc.write(err.write(sc.allocate(), sc));
//			// err(err.errno, com.baidu.hsb.util.StringUtil.decode(err.message,
//			// mc.getCharset()));
//
//			// 全局rollback,可要可不要，因为客户端也会发起
//			// ss.getSource().detectRollback();
//			break;
		case 3:
			// 如果rollback err暂时不用管
			LOGGER.info("xid:" + xaSession.getXid() + " xa rollback error:" + err.errno + ",msg:"
					+ com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()));
			sc.write(err.write(sc.allocate(), sc));
			xaSession.release();
			break;
		}
		mc.setRunning(false);
		endRunning();

	}

	protected void handleSuc(RouteResultsetNode rrn, BlockingSession ss, MySQLChannel mc, BinaryPacket bin,
			ServerConnection sc, AtomicInteger ai, String stmt, int flag, int op) {
		// 成功后
		if (ai.decrementAndGet() == 0) {
			bin.packetId = ++packetId;// OK_PACKET
			// set lastInsertId
			setLastInsertId(bin, sc);
			// 回写
//			if (!sucNeedAck(stmt, op)) {
//				return;
//			}

			// 此处后台做一些额外操作
			XASession xaSession = ss.getSource().getXaSession();
			if (xaSession == null) {
				this.handleError(ErrorCode.ER_XA_CONTEXT_MISSING, "xa session lost", ss);
				return;
			}
			switch (op) {
			// xa start ok
			case 0:
				// 回写ok
				xaSession.setStatus(XAOp.START);
				sc.write(bin.write(sc.allocate(), sc));
				// 此处不能结束mysqlchhanel
				endRunning();
				return;
			// end & pre ok
			case 1:
				try {
					// 存储至redis
					xaSession.saveStore("prepare");
					xaSession.setStatus(XAOp.END_PREPARE);
					// 此处已经代表分布式事务整体成功

					LOGGER.info("xid:" + xaSession.getXid() + " xa end&prepare suc!");

				} catch (Exception e) {
					LOGGER.error("save redis fail", e);
					//
					writeError(ss, ErrorCode.ER_XA_SAVE_REDIS_ERROR, "save redis error");
					break;
					// 发起rollback，此处可异步
					// ss.getSource().selfRollback();
				} finally {
					endRunning();
					mc.setRunning(false);
				}

				try {
					// 成功后就commit,这里其实commit结果已经不重要,为了维持对上的复用，还是选择在commit之后 writeOK
					xaCommit(rrn, ss, flag);
				} catch (Exception e) {
					LOGGER.error("commit request error", e);
					// 此处不需要rollback，只需要异步来恢复即可
				}
				break;
			// xa commit suc
			case 2:
				try {
					// 这里删除key或保存key都可以
					// xaSession.delKey();
					xaSession.saveStore("commit");
					xaSession.setStatus(XAOp.COMMIT);
					xaSession.release();
					// 即使没有回写成功也可以
					ss.writeOk();
					// log
					LOGGER.info("xid:" + xaSession.getXid() + " xa commit suc!");
				} catch (Exception e) {
					LOGGER.error("commit save error", e);
					// 此处不需要rollback，只需要异步来恢复即可
				}
				break;
			// xa rollback suc
			case 3:
				// 回写错误码
				xaSession.setStatus(XAOp.ROLLBACK);
				xaSession.release();
				ss.writeOk();
				LOGGER.info("xid:" + xaSession.getXid() + " xa rollback suc!");
				// writeError(ss);
				break;
			}
			endRunning();
			mc.setRunning(false);
		}

	}

	private void writeError(BlockingSession ss, int code, String msg) {
		endRunning();
		// 清理
//		ss.clear();
		ServerConnection sc = ss.getSource();
		// sc.setTxInterrupt();
		// 通知
		ErrorPacket err = new ErrorPacket();
		err.packetId = ++packetId;// ERROR_PACKET
		err.errno = code;
		err.message = com.baidu.hsb.util.StringUtil.encode(msg, sc.getCharset());
		sc.write(err.write(sc.allocate(), sc));
	}

	// 提交之
	private void xaCommit(RouteResultsetNode rrn, BlockingSession ss, int flag) {
		// xa commit
		execute(rrn, ss, flag, "", 2);
	}

	/**
	 * 非语句性执行异常处理
	 */
	protected void handleError(int errno, String message, BlockingSession ss) {
		endRunning();

		// 清理
//		ss.clear();

		ServerConnection sc = ss.getSource();
		sc.setTxInterrupt();

		// 通知
		ErrorPacket err = new ErrorPacket();
		err.packetId = ++packetId;// ERROR_PACKET
		err.errno = errno;
		err.message = com.baidu.hsb.util.StringUtil.encode(message, sc.getCharset());
		sc.write(err.write(sc.allocate(), sc));
		// sc.writeCode(false, errno);
	}

}
