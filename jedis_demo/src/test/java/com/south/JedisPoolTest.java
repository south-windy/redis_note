package com.south;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * @author 南风
 * @name JedisPoolTest
 * @date 2023-05-31 11:22
 */
public class JedisPoolTest {

    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //最大连接
        jedisPoolConfig.setMaxTotal(10);
        //最大空闲连接
        jedisPoolConfig.setMaxIdle(10);
        //最小空闲连接
        jedisPoolConfig.setMinIdle(10);
        // 设置最长等待时间 ms：毫秒
        //jedisPoolConfig.setMaxWaitMillis(200);
        //上面方法过时就用下面的
        jedisPoolConfig.setMaxWait(Duration.ofMillis(200));
//        jedisPool = new JedisPool(jedisPoolConfig,"127.0.0.1",6379,1000,"123456");
        jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 1000);
    }

    /**
     * 获取jedis
     * @return
     */
    public static Jedis getJedisResource() {
        return jedisPool.getResource();
    }
}
