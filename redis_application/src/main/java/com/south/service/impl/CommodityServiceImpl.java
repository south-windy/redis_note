package com.south.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.south.constant.RedisConstant;
import com.south.dto.RedisBaseData;
import com.south.mapper.CommodityInfoMapper;
import com.south.model.CommodityInfo;
import com.south.service.CommodityService;
import com.south.utils.RedisUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 南风
 * @name CommodityServiceImpl
 * @date 2024-05-13 17:23
 */
@Service
public class CommodityServiceImpl implements CommodityService {

    @Resource
    private CommodityInfoMapper commodityInfoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisUtils redisUtils;

    private static final ExecutorService REBUILDING_CACHE_THREAD_POOL = Executors.newFixedThreadPool(10);

    @Override
    public CommodityInfo getCommodityInfo(Long id) {
        // return getCommodity3(id);
        CommodityInfo commodityInfo = redisUtils.getCachePenetration(RedisConstant.COMMODITY_INFO_KEY, id, CommodityInfo.class, commodityInfoMapper::selectByPrimaryKey, 60L, TimeUnit.SECONDS);
        return commodityInfo;
    }


    /**
     * 未解决穿透问题
     *
     * @param id
     * @return
     */
    private CommodityInfo getCommodity1(Long id) {
        //查询Redis中是否有缓存，有则返回
        String commodityInfo = stringRedisTemplate.opsForValue().get(RedisConstant.COMMODITY_INFO_KEY + id);
        if (StrUtil.isNotBlank(commodityInfo)) {
            CommodityInfo result = JSONUtil.toBean(commodityInfo, CommodityInfo.class);
            return result;
        }
        //查询数据库，未查询到返回异常
        CommodityInfo result = commodityInfoMapper.selectByPrimaryKey(id);
        if (result == null) {
            stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, "", RedisConstant.COMMODITY_INFO_KEY_MINUTES_TTL_KEY, TimeUnit.MINUTES);
            return null;
        }
        //缓存到Redis中
        stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, JSONUtil.toJsonStr(result), RedisConstant.COMMODITY_INFO_KEY_MINUTES_TTL_KEY, TimeUnit.MINUTES);
        return result;
    }

    /**
     * 通过互斥锁锁方式解决缓存击穿问题
     *
     * @param id
     * @return
     */
    private CommodityInfo getCommodity2(Long id) {
        //查询Redis中是否有缓存，有则返回
        String commodityInfo = stringRedisTemplate.opsForValue().get(RedisConstant.COMMODITY_INFO_KEY + id);

        if (StrUtil.isNotBlank(commodityInfo)) {
            CommodityInfo result = JSONUtil.toBean(commodityInfo, CommodityInfo.class);
            return result;
        }

        //判断是否是空值
        if (commodityInfo != null) {
            return null;
        }

        //1.缓存重建
        try {
            //1.1获取互斥锁  判断是否获取成功
            if (!tryLock(RedisConstant.COMMODITY_INFO_LOCK_KEY + id)) {
                //失败，休眠并重试
                Thread.sleep(200);
                return getCommodity2(id);
            }
            //查询数据库，未查询到返回异常
            CommodityInfo result = commodityInfoMapper.selectByPrimaryKey(id);
            //模拟延迟
            Thread.sleep(500);
            if (result == null) {
                stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, "", RedisConstant.COMMODITY_INFO_KEY_MINUTES_TTL_KEY, TimeUnit.MINUTES);
                return null;
            }
            //缓存到Redis中
            stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, JSONUtil.toJsonStr(result), RedisConstant.COMMODITY_INFO_KEY_MINUTES_TTL_KEY, TimeUnit.MINUTES);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unLock(RedisConstant.COMMODITY_INFO_LOCK_KEY + id);
        }
        return null;
    }

    /**
     * 通过逻辑过期方式解决缓存击穿问题
     *
     * @param id
     * @return
     */
    private CommodityInfo getCommodity3(Long id) {
        //查询Redis中是否有缓存，有则返回
        String redisBaseData = stringRedisTemplate.opsForValue().get(RedisConstant.COMMODITY_INFO_KEY + id);

        //不存在直接返回
        if (StrUtil.isBlank(redisBaseData)) {
            return null;
        }
        //命中则反序列化
        RedisBaseData redisData = JSONUtil.toBean(redisBaseData, RedisBaseData.class);
        //判断是否过期
        if (redisData.getExpirationDate().isAfter(LocalDateTime.now())) {
            //未过期直接返回
            return JSONUtil.toBean((JSONObject) redisData.getData(), CommodityInfo.class);
        }
        //过期，先返回过期数据，新线程重建缓存
        //1.缓存重建

        //1.1获取互斥锁  判断是否获取成功
        if (!tryLock(RedisConstant.COMMODITY_INFO_LOCK_KEY + id)) {
            //失败，直接返回已经逻辑过期的数据
            return JSONUtil.toBean((JSONObject) redisData.getData(), CommodityInfo.class);
        }
        //获取锁成功，再次检查数据是否过期
        String againRedisBaseData = stringRedisTemplate.opsForValue().get(RedisConstant.COMMODITY_INFO_KEY + id);
        RedisBaseData againRedisData = JSONUtil.toBean(againRedisBaseData, RedisBaseData.class);
        if (againRedisData.getExpirationDate().isAfter(LocalDateTime.now())) {
            return JSONUtil.toBean((JSONObject) againRedisData.getData(), CommodityInfo.class);
        }
        //新线程重建缓存
        REBUILDING_CACHE_THREAD_POOL.submit(() -> {
            try {
                this.setCommodityInfoCache(id, RedisConstant.COMMODITY_INFO_KEY_MINUTES_TTL_KEY);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                unLock(RedisConstant.COMMODITY_INFO_LOCK_KEY + id);
            }

        });
        //返回旧数据
        return JSONUtil.toBean((JSONObject) againRedisData.getData(), CommodityInfo.class);
    }

    public void setCommodityInfoCache(Long id, Long expireSeconds) {
        CommodityInfo commodityInfo = commodityInfoMapper.selectByPrimaryKey(id);
        RedisBaseData redisData = new RedisBaseData();
        redisData.setExpirationDate(LocalDateTime.now().plusSeconds(expireSeconds));
        redisData.setData(commodityInfo);
        try {
            Thread.sleep(200);
        } catch (Exception e) {

        }
        stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 获取锁
     *
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstant.COMMODITY_INFO_LOCK_KEY_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(absent);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void unLock(String key) {
        Boolean absent = stringRedisTemplate.delete(key);
    }
}
