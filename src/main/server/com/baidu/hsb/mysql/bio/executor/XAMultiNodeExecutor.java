/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.mysql.bio.executor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.baidu.hsb.config.ErrorCode;
import com.baidu.hsb.config.util.ByteUtil;
import com.baidu.hsb.config.util.LoggerUtil;
import com.baidu.hsb.mysql.PacketUtil;
import com.baidu.hsb.mysql.bio.Channel;
import com.baidu.hsb.mysql.bio.MySQLChannel;
import com.baidu.hsb.mysql.bio.executor.MultiNodeTask.ErrInfo;
import com.baidu.hsb.mysql.xa.XAOp;
import com.baidu.hsb.net.mysql.BinaryPacket;
import com.baidu.hsb.net.mysql.EOFPacket;
import com.baidu.hsb.net.mysql.ErrorPacket;
import com.baidu.hsb.net.mysql.FieldPacket;
import com.baidu.hsb.net.mysql.MySQLPacket;
import com.baidu.hsb.net.mysql.OkPacket;
import com.baidu.hsb.route.RouteResultset;
import com.baidu.hsb.route.RouteResultsetNode;
import com.baidu.hsb.server.ServerConnection;
import com.baidu.hsb.server.parser.ServerParse;
import com.baidu.hsb.server.session.BlockingSession;
import com.baidu.hsb.server.session.XASession;
import com.baidu.hsb.util.StringUtil;

/**
 * 多节点多sql任务器
 * 
 * @author brucexx
 *
 */
public class XAMultiNodeExecutor {

	protected static boolean IS_DEBUG = true;
	protected static final Logger LOGGER = Logger.getLogger(XAMultiNodeExecutor.class);
	protected static final int RECEIVE_CHUNK_SIZE = 16 * 1024;

	protected AtomicBoolean isFail = new AtomicBoolean(false);
	protected int unfinishedNodeCount;

	protected AtomicBoolean fieldEOF = new AtomicBoolean(false);
	protected byte packetId;
	protected long affectedRows;
	protected long insertId;
	protected ByteBuffer buffer;
	protected ReentrantLock lock = new ReentrantLock();
	protected Condition taskFinished = lock.newCondition();
	protected DefaultCommitExecutor icExecutor = new DefaultCommitExecutor() {
		@Override
		protected String getErrorMessage() {
			return "Internal commit";
		}

		@Override
		protected Logger getLogger() {
			return MultiNodeTask.LOGGER;
		}

	};
	protected long nodeCount = 0;
	protected long totalCount = 0;

	protected AtomicLong exeTime;
	protected RouteResultsetNode[] nodes;
	protected BlockingSession ss;
	protected int flag;
	protected String oSql;
	protected Map<String, AtomicInteger> runData = new HashMap<String, AtomicInteger>();
	protected byte[] funcCachedData;
	protected int type;
	protected int op;
	int errno;
	String errMessage;

	protected void init(RouteResultsetNode[] nodes, final BlockingSession ss, final int flag, int type, int op) {

		String xId = ss.getSource().getXaSession().getXid();

		this.op = op;
		this.nodes = nodes;
		this.ss = ss;
		this.flag = flag;
		this.type = type;
		this.funcCachedData = new byte[0];
		this.isFail.set(false);
		this.unfinishedNodeCount = 0;
		this.nodeCount = 0;
		// 重新组织rrn nodes里的sql
		XAUtil.genMulti(op, xId, nodes);

		totalCount = unfinishedNodeCount;

		this.fieldEOF.set(false);

		this.packetId = 0;
		this.affectedRows = 0L;
		this.insertId = 0L;
		this.buffer = ss.getSource().allocate();
		exeTime = new AtomicLong(0);

	}

	/**
	 * 
	 * @param sql
	 */
	public XAMultiNodeExecutor() {
	}

	public void setSql(String sql) {
		this.oSql = sql;
	}

	public void execute(RouteResultsetNode[] nodes, final BlockingSession ss, final int flag, int type, int op) {
		init(nodes, ss, flag, type, op);
		String xId = ss.getSource().getXaSession().getXid();
		LOGGER.info("xId:" + xId + "," + (XAUtil.convert(op)));

		if (ss.getSource().isClosed()) {
			decrementCountToZero();
			ss.getSource().recycle(this.buffer);
			return;
		}

		RouteResultsetNode[] newNodes = XAUtil.genNodesByAddr(nodes);
		for (RouteResultsetNode rrn : newNodes) {
			unfinishedNodeCount += rrn.getSqlCount();
			this.nodeCount++;
			runData.put(rrn.getName(), new AtomicInteger(rrn.getSqlCount()));
		}

		ThreadPoolExecutor exec = ss.getSource().getProcessor().getExecutor();
		for (final RouteResultsetNode rrn : newNodes) {
			final Channel c = ss.getTarget().get(rrn);
			if (c != null && !c.isClosed()) {
				if (op == 1) {
					exec.execute(new Runnable() {
						@Override
						public void run() {
							execute0(rrn, c, ss, flag, exeTime);
						}
					});
				} else {
					if (!c.isRunning()) {
						exec.execute(new Runnable() {
							@Override
							public void run() {
								execute0(rrn, c, ss, flag, exeTime);
							}
						});
					} else {
						if (IS_DEBUG) {
							//
							LOGGER.info("op[" + op + "]新建之0。。。");
						}
						newExecute(rrn, ss, flag, exeTime);
					}
				}

			} else {
				if (IS_DEBUG) {
					//
					LOGGER.info("op[" + op + "]新建之1。。。");
				}
				newExecute(rrn, ss, flag, exeTime);
			}
		}
	}

	/**
	 * 新通道的执行
	 */
	protected void newExecute(final RouteResultsetNode rrn, final BlockingSession ss, final int flag,
			final AtomicLong exeTime) {
		final ServerConnection sc = ss.getSource();

		// 提交执行任务
		sc.getProcessor().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					runTask(rrn, ss, sc, flag, exeTime);
				} catch (Exception e) {
					killServerTask(rrn, ss);
					failFinish(ss, ErrorCode.ER_MULTI_EXEC_ERROR, e.getMessage());
				}

			}
		});
	}

	protected void killServerTask(RouteResultsetNode rrn, BlockingSession ss) {
		ConcurrentMap<RouteResultsetNode, Channel> target = ss.getTarget();
		Channel c = target.get(rrn);
		if (c != null) {
			c.kill();
		}
	}

	/**
	 * 执行
	 */
	protected void execute0(RouteResultsetNode rrn, Channel c, BlockingSession ss, int flag, final AtomicLong exeTime) {
		if (IS_DEBUG) {
			//
			LOGGER.info("op[" + op + "]复用之。。。");
		}

		ServerConnection sc = ss.getSource();
		// 中间有中断就不执行了。。
		if (isFail.get() || sc.isClosed()) {
			// failFinish(ss, errno, errMessage);
			// c.setRunning(false);
			decrementCount(rrn.getSqlCount());
			if (op > 0) {
				c.setRunning(false);
			}
			return;
		}
		long s = System.currentTimeMillis();
		int ac = 0;
		extSql: for (final String stmt : rrn.getStatement()) {

			// 这里再reuse op==1 必须要命中op==0执行过的连接，如果不是之前执行过的连接选择跳过
//			if (op == 1) {
//				if (!ss.getSource().getXaSession().xaMustReuse(c, stmt, flag)) {
//					decrementCountBy(1);
//					continue;
//				}
//			}
			// xa 操作对于同一物理地址不可重复执行
//			if (!ss.getSource().getXaSession().canRepeat(((MySQLChannel) c), stmt)) {
//				decrementCount(1);
//				continue;
//			}
			if (IS_DEBUG) {
				LOGGER.info(rrn.getName() + "," + "stmt:" + stmt + ",execute-->" + ((MySQLChannel) c).getId()
						+ ",unfinished->" + unfinishedNodeCount);
			}
			// op==0时 先buf conn
			// ss.getSource().getXaSession().bufConn(c, stmt, flag);

			try {
				if (isFail.get() || sc.isClosed()) {
					//
					decrementCount(rrn.getSqlCount() - ac);
					// 此处op=0以后必须要复用
					if (op > 0) {
						c.setRunning(false);
					}
					return;
				}

				// 执行并等待返回
				BinaryPacket bin = ((MySQLChannel) c).execute(stmt, rrn, sc, false);
				ac++;
				// 接收和处理数据
				final ReentrantLock lock = XAMultiNodeExecutor.this.lock;
				lock.lock();
				try {
					switch (bin.data[0]) {
					case ErrorPacket.FIELD_COUNT:
						if (handleFailure(stmt, (MySQLChannel) c, ss, rrn, bin, null, exeTime)) {
							continue;
						} else {
							break;
						}
					case OkPacket.FIELD_COUNT:
						OkPacket ok = new OkPacket();
						ok.read(bin);
						affectedRows += ok.affectedRows;
						if (ok.insertId > 0) {
							insertId = (insertId == 0) ? ok.insertId : Math.min(insertId, ok.insertId);
						}
						handleSuccessOK((MySQLChannel) c, ss, rrn, ok);
						break;
					default: // HEADER|FIELDS|FIELD_EOF|ROWS|LAST_EOF
						final MySQLChannel mc = (MySQLChannel) c;
						if (fieldEOF.get()) {
							for (;;) {
								bin = mc.receive();
								switch (bin.data[0]) {
								case ErrorPacket.FIELD_COUNT:
									// 此处不会忽略，因为xa系列都是非rows
									handleFailure(stmt, (MySQLChannel) c, ss, rrn, bin, null, exeTime);
									continue extSql;
								case EOFPacket.FIELD_COUNT:
									handleRowData(rrn, (MySQLChannel) c, ss, exeTime, stmt);
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
									handleFailure(stmt, (MySQLChannel) c, ss, rrn, bin, null, exeTime);
									continue extSql;
								case EOFPacket.FIELD_COUNT:
									bin.packetId = ++packetId;// FIELD_EOF
									for (MySQLPacket packet : headerList) {
										buffer = packet.write(buffer, sc);
									}
									headerList = null;
									buffer = bin.write(buffer, sc);
									fieldEOF.set(true);
									handleRowData(rrn, (MySQLChannel) c, ss, exeTime, stmt);
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
				}
			} catch (final Exception e) {
				LOGGER.error("exception  " + ss.getSource() + ",sql[" + stmt + "]", e);
				handleFailure(stmt, (MySQLChannel) c, ss, rrn, null, new SimpleErrInfo(e, ErrorCode.ER_YES, sc, rrn),
						exeTime);
				c.close();
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
	protected void handleSuccessOK(MySQLChannel c, BlockingSession ss, RouteResultsetNode rrn, OkPacket ok) {
		if (decrementCountAndIsZero(1)) {
			if (isFail.get()) {
				writeError(ss, errno, errMessage);
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

				sucXaOp(c, ss, rrn, op, exeTime, new Runback() {
					@Override
					public void g() {
						ok.write(source);
						source.recycle(buffer);
					}
				});

			} catch (Exception e) {
				LOGGER.warn("exception happens in success notification: " + ss.getSource(), e);
			}
		}
	}

	protected void handleSuccessEOF(String stmt, MySQLChannel c, BlockingSession ss, final RouteResultsetNode rrn,
			BinaryPacket bin, final AtomicLong exeTime) {

		if (decrementCountAndIsZero(1)) {
			try {
				if (isFail.get()) {
					writeError(ss, errno, errMessage);
					return;
				}
				try {
					ServerConnection source = ss.getSource();
					// 忽略自动提交
//					if (source.isAutocommit()) {
//						ss.release();
//					}
					if (flag == RouteResultset.SUM_FLAG || flag == RouteResultset.MAX_FLAG
							|| flag == RouteResultset.MIN_FLAG) {
						BinaryPacket data = new BinaryPacket();
						data.packetId = ++packetId;
						data.data = funcCachedData;
						buffer = data.write(buffer, source);
					}
					bin.packetId = ++packetId;// LAST_EOF

					sucXaOp(c, ss, rrn, op, exeTime, new Runback() {
						@Override
						public void g() {
							source.write(bin.write(buffer, source));
							source.recycle(buffer);
						}
					});

				} catch (Exception e) {
					LOGGER.warn("exception happens in success notification: " + ss.getSource(), e);
				}
			} finally {

				LoggerUtil.printDigest(LOGGER, (exeTime.get() / nodeCount),
						com.baidu.hsb.route.util.StringUtil.join(rrn.getStatement(), '#'));
			}
		}
	}

	/**
	 * 成功操作
	 * 
	 * @param ss
	 * @param bin
	 * @param op
	 */
	private void sucXaOp(MySQLChannel c, BlockingSession ss, final RouteResultsetNode rrn, int op,
			final AtomicLong exeTime, Runback r) {
		// 此处后台做一些额外操作
		XASession xaSession = ss.getSource().getXaSession();
		if (xaSession == null) {
			writeError(ss, ErrorCode.ER_XA_CONTEXT_MISSING, "xa session lost");
			return;
		}
		switch (op) {
		// xa start ok
		case 0:
			// 回写ok
			xaSession.setStatus(XAOp.START);
			r.g();
			// 此处先不停mysqlchannel
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
				writeError(ss, ErrorCode.ER_XA_SAVE_REDIS_ERROR, "save redis error");
				//
				// writeError(ss, ErrorCode.ER_XA_SAVE_REDIS_ERROR, "save redis error");
				break;
				// 发起rollback，此处可异步
				// ss.getSource().selfRollback();
			} finally {
				/**
				 * 
				 * if (runData.get(rrn.getName()).decrementAndGet() == 0) { c.setRunning(false);
				 * }
				 */

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
				// 代表
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
		//
		c.setRunning(false);

	}

	// 提交之
	private void xaCommit(RouteResultsetNode rrn, BlockingSession ss, int flag) {
		// xa commit
		execute(this.nodes, ss, flag, ServerParse.XA, 2);
	}

	protected void handleRowData(final RouteResultsetNode rrn, final MySQLChannel c, BlockingSession ss,
			final AtomicLong exeTime, String stmt) throws IOException {
		final ServerConnection source = ss.getSource();
		BinaryPacket bin = null;
		for (;;) {
			bin = ((MySQLChannel) c).receive();
			switch (bin.data[0]) {
			case ErrorPacket.FIELD_COUNT:
				handleFailure(stmt, c, ss, rrn, bin, null, exeTime);
				return;
			case EOFPacket.FIELD_COUNT:
				handleSuccessEOF(stmt, c, ss, rrn, bin, exeTime);
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

	private boolean failNeedAck(String stmt, int errno, int op) {
		if (!XAUtil.isXAOp(stmt)) {
			return true;
		}
		// rollback失败必须要返回 //过程commit中有失败呢？直接让上层超时吧
		if (op == 3 || op == 1) {
			return true;
		}

		if (XAUtil.isXAOp(stmt) && XAUtil.isXAIgnore(errno)) {
			return false;
		}

		return true;
	}

	protected void runTask(final RouteResultsetNode rrn, final BlockingSession ss, final ServerConnection sc,
			final int flag, final AtomicLong exeTime) {
		// 取得数据通道
		int i = rrn.getReplicaIndex();
		Channel c = null;
		try {
			c = ss.getSource().getXaSession().getChannel(rrn, i);
		} catch (final Exception e) {
			LOGGER.error("get channel error", e);
			failFinish(ss, ErrorCode.ER_BAD_DB_ERROR, "ER_BAD_DB_ERROR");
			return;
		}

		c.setRunning(true);
		Channel old = ss.getTarget().put(rrn, c);
		if (old != null && c != old) {
			old.close();
		}

		// 执行
		execute0(rrn, c, ss, flag, exeTime);
	}

	private ErrorPacket g(BinaryPacket bin) {
		ErrorPacket err = new ErrorPacket();
		err.read(bin);
		return err;
	}

	/**
	 * 如果出错，通过isFail在执行层包住 ,返回是否可忽略
	 * 
	 * @param stmt
	 * @param c
	 * @param ss
	 * @param rrn
	 * @param bin
	 * @param errInfo
	 * @param exeTime
	 */
	protected boolean handleFailure(String stmt, MySQLChannel c, BlockingSession ss, RouteResultsetNode rrn,
			BinaryPacket bin, ErrInfo errInfo, final AtomicLong exeTime) {
		String sSql = com.baidu.hsb.route.util.StringUtil.join(rrn.getStatement(), '#');

		try {
			ErrorPacket err = bin != null ? g(bin) : null;
			int errno = err != null ? err.errno : errInfo.getErrNo();
			String errmsg = err != null ? StringUtil.decode(err.message, c.getCharset()) : errInfo.getErrMsg();

			// 回写错误码
			if (!failNeedAck(stmt, errno, op)) {
				if (XAUtil.isXAIgnore(errno)) {
					// 忽略错误
					decrementCountBy(1);
					return true;
				} else {
					// 有错误挺好
					decrementCountBy(rrn.getSqlCount());
				}
				return false;
			}

			// 此处后台做一些额外操作
			XASession xaSession = ss.getSource().getXaSession();
			if (xaSession == null) {
				writeError(ss, ErrorCode.ER_XA_CONTEXT_MISSING, "xa session lost");
				return false;
			}
			if (op == 2) {
				LOGGER.info("xid:" + xaSession.getXid() + " xa commit error :" + errno + ",msg:" + errmsg);
				xaSession.release();
			}

			// 标记为执行失败，并记录第一次异常信息。
			if (!isFail.getAndSet(true)) {
				this.errno = errno;
				this.errMessage = errmsg;
				LOGGER.warn(
						rrn.getName() + " error[" + err + "," + errmsg + "] in sql[" + stmt + "]node:" + rrn.getName());
			} else {
				// 如果有并发过来，直接忽略
				return false;
			}

			// 此处做一些额外操作
			switch (op) {
			// sql错误,给客户端回滚
			case 0:
				LOGGER.info("xid:" + xaSession.getXid() + " xa start error:" + errno + ",msg:" + errmsg);
				// 回写原错误，上层有回滚，下面也有回滚
				notifyFailure(ss, err);
				// 此处不能结束mysqlchannel
				return false;
			// end&prepare 错误 客户端发起回滚，如果客户端回滚不成异步recover(这里可能不需要)
			case 1:
				LOGGER.info("xid:" + xaSession.getXid() + " xa end&prepare error:" + errno + ",msg:" + errmsg);
				// 回写原错误，上层有回滚，下面也有回滚
				notifyFailure(ss, err);
				break;
			// 内部 commit error，不需要管了，异步recover
//			case 2:
//				LOGGER.info("xid:" + xaSession.getXid() + " xa commit error :" + err.errno + ",msg:"
//						+ com.baidu.hsb.util.StringUtil.decode(err.message, mc.getCharset()));
//				// sc.write(err.write(sc.allocate(), sc));
//				// err(err.errno, com.baidu.hsb.util.StringUtil.decode(err.message,
//				// mc.getCharset()));
			//
//				// 全局rollback,可要可不要，因为客户端也会发起
//				// ss.getSource().detectRollback();
//				break;
			case 3:
				LOGGER.info("xid:" + xaSession.getXid() + " xa rollback error:" + errno + ",msg:" + errmsg);
				// 如果rollback err暂时不用管
				notifyFailure(ss, err);
				xaSession.release();
				break;
			}
			c.setRunning(false);

		} catch (Exception e) {
			LOGGER.warn("handleFailure failed in " + getClass().getSimpleName() + ", source = " + ss.getSource(), e);
		} finally {
			LoggerUtil
					.printDigest(LOGGER,
							(long) (exeTime.get()
									/ ((double) (totalCount - unfinishedNodeCount) * nodeCount / (double) totalCount)),
							sSql);
		}
		return false;
	}

	protected void failFinish(BlockingSession ss, int errno, String errMessage) {
		forceFinish();
		writeError(ss, errno, errMessage);
	}

	protected void notifyFailure(BlockingSession ss, ErrorPacket bin) {
		try {
			// 不需要清理
			// ss.clear();
			ServerConnection sc = ss.getSource();
			// 清空buf
			sc.recycle(this.buffer);
			sc.write(bin.write(sc.allocate(), sc));
			// sc.write(bin.write(buffer, sc));

		} catch (Exception e) {
			LOGGER.warn("exception happens in failure notification: " + ss.getSource(), e);
		}
	}

	/**
	 * 通知，执行异常
	 * 
	 * @throws nothing never throws any exception
	 */
	protected void writeError(BlockingSession ss, int errno, String errMessage) {
		try {
			// 不需要清理
			// ss.clear();
			ServerConnection sc = ss.getSource();
			sc.setTxInterrupt();

			// 通知
			ErrorPacket err = new ErrorPacket();
			err.packetId = ++packetId;// ERROR_PACKET
			err.errno = errno;
			err.message = StringUtil.encode(errMessage, sc.getCharset());
			// 清空buf
			sc.recycle(this.buffer);

			sc.write(err.write(buffer, sc));

		} catch (Exception e) {
			LOGGER.warn("exception happens in failure notification: " + ss.getSource(), e);
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

	protected void decrementCount(int c) {
		unfinishedNodeCount = unfinishedNodeCount - c;
	}

	protected void decrementCountToZero() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			unfinishedNodeCount = 0;
			taskFinished.signalAll();
		} finally {
			lock.unlock();
		}
	}

	protected void forceFinish() {
		decrementCountToZero();
	}

	protected void decrementCountBy(int c) {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			unfinishedNodeCount = unfinishedNodeCount - c;
		} finally {
			lock.unlock();
		}
	}

	protected boolean decrementCountAndIsZero(int c) {
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

	protected static interface Runback {
		void g();
	}

	protected static class BinaryErrInfo implements ErrInfo {
		private String errMsg;
		private int errNo;
		private ServerConnection source;
		private RouteResultsetNode rrn;
		private MySQLChannel mc;

		public BinaryErrInfo(MySQLChannel mc, BinaryPacket bin, ServerConnection sc, RouteResultsetNode rrn) {
			this.mc = mc;
			this.source = sc;
			this.rrn = rrn;
			ErrorPacket err = new ErrorPacket();
			err.read(bin);
			this.errMsg = (err.message == null) ? null : StringUtil.decode(err.message, mc.getCharset());
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
		private Exception e;
		private int errNo;
		private String msg;
		private ServerConnection source;
		private RouteResultsetNode rrn;

		public SimpleErrInfo(Exception e, int errNo, ServerConnection sc, RouteResultsetNode rrn) {
			this(e, errNo, null, sc, rrn);
		}

		public SimpleErrInfo(Exception e, int errNo, String msg, ServerConnection sc, RouteResultsetNode rrn) {
			this.e = e;
			this.errNo = errNo;
			this.source = sc;
			this.rrn = rrn;
			this.msg = msg;
		}

		@Override
		public int getErrNo() {
			return errNo;
		}

		@Override
		public String getErrMsg() {
			String msg = e == null ? this.msg : e.getMessage();
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
