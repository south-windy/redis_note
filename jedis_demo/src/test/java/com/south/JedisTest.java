package com.south;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;


/**
 * Unit test for simple App.
 */
public class JedisTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testString() {
        jedis.set("key1", "zhangsan");
        System.out.println(jedis.get("testKey"));
    }

    @Test
    public void testHash() {
        jedis.hset("user:1", "name","张三");
        jedis.hset("user:1", "age","28");
        System.out.println(jedis.hgetAll("user:1"));
    }

    @Test
    public void testJedisPool() {
        jedis = JedisPoolTest.getJedisResource();
        System.out.println(jedis.hgetAll("user:1"));
    }

    private Jedis jedis = null;

    @BeforeEach
    private void loadingRedis() {
        //连接地址
        jedis = new Jedis("127.0.0.1", 6379);
        //设置密码
//        jedis.auth("123456");
        //选择库
        jedis.select(0);
    }

    @AfterEach
    private void closeRedis() {
        //释放资源
        if (jedis != null) {
            jedis.close();
        }
    }

}
