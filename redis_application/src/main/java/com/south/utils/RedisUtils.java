package com.south.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.south.constant.RedisConstant;
import com.south.dto.RedisBaseData;
import com.south.model.CommodityInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.lang.FunctionMapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis工具类
 *
 * @author 南风
 * @name RedisUtils
 * @date 2024-09-12 17:10
 */
@Slf4j
@Component
public class RedisUtils {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService REBUILDING_CACHE_THREAD_POOL = Executors.newFixedThreadPool(10);


    /**
     * 将数据放入redis
     *
     * @param key      key
     * @param value    值
     * @param time     过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    /**
     * 设置逻辑过期缓存
     *
     * @param key
     * @param value
     * @param time
     * @param timeUnit
     */
    public void setLogicalExpireCache(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisBaseData redisData = new RedisBaseData();
        redisData.setData(value);
        redisData.setExpirationDate(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 利用缓存空值解决缓存穿透问题
     *
     * @param keyPrefix
     * @param param
     * @param returnType
     * @param findData
     * @param time
     * @param timeUnit
     * @param <R>
     * @param <Param>
     * @return
     */
    public <R, Param> R getCachePenetration(String keyPrefix, Param param, Class<R> returnType, Function<Param, R> findData, Long time, TimeUnit timeUnit) {
        String key = keyPrefix + param;
        String json = stringRedisTemplate.opsForValue().get(key);
        //不为 null和空字符串时直接返回
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, returnType);
        }
        //
        if (json != null) {
            return null;
        }
        R data = findData.apply(param);
        //数据不存在存空字符串
        if (data == null) {
            //空值缓存时间为60秒
            this.set(key, "", RedisConstant.NULL_DATA_TTL, TimeUnit.SECONDS);
            return null;
        }
        this.set(key, data, time, timeUnit);
        return data;
    }


    /**
     * 通过逻辑过期解决缓存击穿问题（热点KEY）
     * @param keyPrefix 前缀
     * @param param 查询数据方法的参数
     * @param returnType 返回值类型
     * @param findData 查询数据的函数
     * @param time 过期时间
     * @param timeUnit 时间单位
     * @return
     * @param <R> 返回值泛型
     * @param <Param> 查询参数泛型
     */
    public <R, Param> R getCacheBreakdown(String keyPrefix, Param param, Class<R> returnType, Function<Param, R> findData, Long time, TimeUnit timeUnit) {
        //查询Redis中是否有缓存，有则返回
        String redisBaseData = stringRedisTemplate.opsForValue().get(keyPrefix + param);

        //不存在直接返回
        if (StrUtil.isBlank(redisBaseData)) {
            return null;
        }
        //命中则反序列化
        RedisBaseData redisData = JSONUtil.toBean(redisBaseData, RedisBaseData.class);
        //判断是否过期
        if (redisData.getExpirationDate().isAfter(LocalDateTime.now())) {
            //未过期直接返回
            return JSONUtil.toBean((JSONObject) redisData.getData(), returnType);
        }
        //过期，先返回过期数据，新线程重建缓存
        //1.缓存重建

        //1.1获取互斥锁  判断是否获取成功
        if (!tryLock(keyPrefix + param)) {
            //失败，直接返回已经逻辑过期的数据
            return JSONUtil.toBean((JSONObject) redisData.getData(), returnType);
        }
        //获取锁成功，再次检查数据是否过期
        String againRedisBaseData = stringRedisTemplate.opsForValue().get(keyPrefix + param);
        RedisBaseData againRedisData = JSONUtil.toBean(againRedisBaseData, RedisBaseData.class);
        if (againRedisData.getExpirationDate().isAfter(LocalDateTime.now())) {
            return JSONUtil.toBean((JSONObject) againRedisData.getData(), returnType);
        }
        //新线程重建缓存
        REBUILDING_CACHE_THREAD_POOL.submit(() -> {
            try {
                R apply = findData.apply(param);
                this.setLogicalExpireCache(keyPrefix + param, apply, time, timeUnit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                unLock(keyPrefix + param);
            }

        });
        //返回旧数据
        return JSONUtil.toBean((JSONObject) againRedisData.getData(), returnType);
    }

    /**
     * 获取锁
     *
     * @param key
     * @return
     */
    public boolean tryLock(String key) {
        Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstant.COMMODITY_INFO_LOCK_KEY_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(absent);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    public void unLock(String key) {
        Boolean absent = stringRedisTemplate.delete(key);
    }
}
