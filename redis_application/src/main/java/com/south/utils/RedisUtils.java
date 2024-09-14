package com.south.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.south.constant.RedisConstant;
import com.south.dto.RedisBaseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
            this.set(key, "", time, timeUnit);
            return null;
        }
        this.set(key, data, time, timeUnit);
        return data;
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
