/**
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.hsb.store;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import com.baidu.hsb.route.util.StringUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;

/**
 * @author brucexx
 *
 */
public class RedisClient {

    private static final Logger logger = Logger.getLogger(RedisClient.class);

    private static final String keyPrefix = "HSB_XA_";
    /**
     * 
     */
    private static ConsistentHash<JedisSentinelPool> hash = new ConsistentHash<JedisSentinelPool>();
    private static JedisPool pool = null;

    public RedisClient(Set<String> masterSet, Set<String> sentinelSet, String pwd) {
        logger.info("masterSet:" + masterSet + ",sentinelSet:" + sentinelSet + ",pwd:" + pwd);
        for (String masterName : masterSet) {
            JedisSentinelPool jsPool = new JedisSentinelPool(masterName, sentinelSet, getConfig(), 2000, pwd);
            hash.add(masterName, jsPool);
        }
    }

    public RedisClient(String uri, String pwd) throws URISyntaxException {
        URI path = new URI(uri);
        pool = new JedisPool(new GenericObjectPoolConfig(), path.getHost(), path.getPort(), 2000, pwd);
    }

    private GenericObjectPoolConfig getConfig() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxWaitMillis(2000);
        return config;
    }

    private static Jedis getResource(String key) {
        if (pool != null) {
            return pool.getResource();
        }
        return hash.get(key).getResource();
    }

    private static void recycleResource(Jedis jedis, String key) {
        if (pool != null) {
            pool.returnResource(jedis);
        } else {
            hash.get(key).returnResource(jedis);
        }
    }

    /**
     * 保存一个小时
     * 
     * @param key
     * @param status
     */
    public static void put(String key, String status) {
        String rKey = keyPrefix + key;
        Jedis jedis = getResource(rKey);
        try {
            jedis.setex(key, 600, status + "," + System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("保存xa id" + rKey + ",s:" + status + "异常", e);
            throw new RuntimeException(e);
        } finally {
            recycleResource(jedis, rKey);
        }
    }

    /**
     * 查询这个事务的状态
     * 
     * @param key
     * @return
     */
    private static String get(String key) {
        String rKey = keyPrefix + key;
        Jedis jedis = getResource(rKey);
        try {
            return jedis.get(rKey);
        } catch (Exception e) {
            logger.error("获取xa id" + rKey + " 异常", e);
            throw new RuntimeException(e);
        } finally {
            recycleResource(jedis, rKey);
        }
    }

    public static String[] getData(String key) {
        String s = get(key);
        return StringUtil.isEmpty(s) ? new String[0] : StringUtil.split(s, ",");
    }

}
