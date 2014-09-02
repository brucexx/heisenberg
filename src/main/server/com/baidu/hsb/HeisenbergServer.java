/**
 * Baidu.com,Inc.
 * Copyright (c) 2000-2013 All Rights Reserved.
 */
package com.baidu.hsb;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

import com.baidu.hsb.config.model.config.SystemConfig;
import com.baidu.hsb.manager.ManagerConnectionFactory;
import com.baidu.hsb.mysql.MySQLDataNode;
import com.baidu.hsb.net.NIOAcceptor;
import com.baidu.hsb.net.NIOConnector;
import com.baidu.hsb.net.NIOProcessor;
import com.baidu.hsb.parser.recognizer.mysql.lexer.MySQLLexer;
import com.baidu.hsb.server.ServerConnectionFactory;
import com.baidu.hsb.statistic.SQLRecorder;
import com.baidu.hsb.util.ExecutorUtil;
import com.baidu.hsb.util.NameableExecutor;
import com.baidu.hsb.util.TimeUtil;

/**
 * @author xiongzhao@baidu.com  
 */
public class HeisenbergServer {
    public static final String            NAME               = "Heisenberg";
    private static final long             LOG_WATCH_DELAY    = 60000L;
    private static final long             TIME_UPDATE_PERIOD = 20L;
    private static final HeisenbergServer INSTANCE           = new HeisenbergServer();
    private static final Logger           LOGGER             = Logger
                                                                 .getLogger(HeisenbergServer.class);

    public static final HeisenbergServer getInstance() {
        return INSTANCE;
    }

    private final HeisenbergConfig config;
    private final Timer            timer;
    private final NameableExecutor managerExecutor;
    private final NameableExecutor timerExecutor;
    private final NameableExecutor initExecutor;
    private final SQLRecorder      sqlRecorder;
    private final AtomicBoolean    isOnline;
    private final long             startupTime;
    private NIOProcessor[]         processors;
    private NIOConnector           connector;
    private NIOAcceptor            manager;
    private NIOAcceptor            server;

    private HeisenbergServer() {
        this.config = new HeisenbergConfig();
        SystemConfig system = config.getSystem();
        MySQLLexer.setCStyleCommentVersion(system.getParserCommentVersion());
        this.timer = new Timer(NAME + "Timer", true);
        this.initExecutor = ExecutorUtil.create("InitExecutor", system.getInitExecutor());
        this.timerExecutor = ExecutorUtil.create("TimerExecutor", system.getTimerExecutor());
        this.managerExecutor = ExecutorUtil.create("ManagerExecutor", system.getManagerExecutor());
        this.sqlRecorder = new SQLRecorder(system.getSqlRecordCount());
        this.isOnline = new AtomicBoolean(true);
        this.startupTime = TimeUtil.currentTimeMillis();
    }

    public HeisenbergConfig getConfig() {
        return config;
    }

    public void beforeStart(String dateFormat) {
        String home = System.getProperty("hsb.home");
        if (home == null) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            LogLog.warn(sdf.format(new Date()) + " [hsb.home] is not set.");
        } else {
            Log4jInitializer.configureAndWatch(home + "/conf/log4j.xml", LOG_WATCH_DELAY);
        }
    }

    public void startup() throws IOException {
        // server startup
        LOGGER.info("===============================================");
        LOGGER.info(NAME + " is ready to startup ...");
        SystemConfig system = config.getSystem();
        timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);

        // startup processors
        LOGGER.info("Startup processors ...");
        int handler = system.getProcessorHandler();
        int executor = system.getProcessorExecutor();
        processors = new NIOProcessor[system.getProcessors()];
        for (int i = 0; i < processors.length; i++) {
            processors[i] = new NIOProcessor("Processor" + i, handler, executor);
            processors[i].startup();
        }
        timer.schedule(processorCheck(), 0L, system.getProcessorCheckPeriod());

        // startup connector
        LOGGER.info("Startup connector ...");
        connector = new NIOConnector(NAME + "Connector");
        connector.setProcessors(processors);
        connector.start();

        // init dataNodes
        Map<String, MySQLDataNode> dataNodes = config.getDataNodes();
        LOGGER.info("Initialize dataNodes ...");
        for (MySQLDataNode node : dataNodes.values()) {
            node.init(1, 0);
        }
        timer.schedule(dataNodeIdleCheck(), 0L, system.getDataNodeIdleCheckPeriod());
        timer.schedule(dataNodeHeartbeat(), 0L, system.getDataNodeHeartbeatPeriod());

        // startup manager
        ManagerConnectionFactory mf = new ManagerConnectionFactory();
        mf.setCharset(system.getCharset());
        mf.setIdleTimeout(system.getIdleTimeout());
        manager = new NIOAcceptor(NAME + "Manager", system.getManagerPort(), mf);
        manager.setProcessors(processors);
        manager.start();
        LOGGER.info(manager.getName() + " is started and listening on " + manager.getPort());

        // startup server
        ServerConnectionFactory sf = new ServerConnectionFactory();
        sf.setCharset(system.getCharset());
        sf.setIdleTimeout(system.getIdleTimeout());
        server = new NIOAcceptor(NAME + "Server", system.getServerPort(), sf);
        server.setProcessors(processors);
        server.start();
        timer.schedule(clusterHeartbeat(), 0L, system.getClusterHeartbeatPeriod());

        // server started
        LOGGER.info(server.getName() + " is started and listening on " + server.getPort());
        LOGGER.info("===============================================");
    }

    public NIOProcessor[] getProcessors() {
        return processors;
    }

    public NIOConnector getConnector() {
        return connector;
    }

    public NameableExecutor getManagerExecutor() {
        return managerExecutor;
    }

    public NameableExecutor getTimerExecutor() {
        return timerExecutor;
    }

    public NameableExecutor getInitExecutor() {
        return initExecutor;
    }

    public SQLRecorder getSqlRecorder() {
        return sqlRecorder;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public boolean isOnline() {
        return isOnline.get();
    }

    public void offline() {
        isOnline.set(false);
    }

    public void online() {
        isOnline.set(true);
    }

    // 系统时间定时更新任务
    private TimerTask updateTime() {
        return new TimerTask() {
            @Override
            public void run() {
                TimeUtil.update();
            }
        };
    }

    // 处理器定时检查任务
    private TimerTask processorCheck() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (NIOProcessor p : processors) {
                            p.check();
                        }
                    }
                });
                timerExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        //初始化是否成功检查
                        Map<String, MySQLDataNode> dataNodes = config.getDataNodes();
                        for (MySQLDataNode node : dataNodes.values()) {
                            node.init(1, 0);
                        }
                    }
                });
            }
        };
    }

    // 数据节点定时连接空闲超时检查任务
    private TimerTask dataNodeIdleCheck() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, MySQLDataNode> nodes = config.getDataNodes();
                        for (MySQLDataNode node : nodes.values()) {
                            node.idleCheck();
                        }
                        Map<String, MySQLDataNode> _nodes = config.getBackupDataNodes();
                        if (_nodes != null) {
                            for (MySQLDataNode node : _nodes.values()) {
                                node.idleCheck();
                            }
                        }
                    }
                });
            }
        };
    }

    // 数据节点定时心跳任务
    private TimerTask dataNodeHeartbeat() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, MySQLDataNode> nodes = config.getDataNodes();
                        for (MySQLDataNode node : nodes.values()) {
                            node.doHeartbeat();
                        }
                    }
                });
            }
        };
    }

    // 集群节点定时心跳任务
    private TimerTask clusterHeartbeat() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, HeisenbergNode> nodes = config.getCluster().getNodes();
                        for (HeisenbergNode node : nodes.values()) {
                            node.doHeartbeat();
                        }
                    }
                });
            }
        };
    }

}
