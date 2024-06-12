package com.south.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.south.constant.RedisConstant;
import com.south.mapper.CommodityInfoMapper;
import com.south.model.CommodityInfo;
import com.south.service.CommodityService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Override
    public CommodityInfo getCommodityInfo(Long id) {
        return getCommodity2(id);
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
            stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, "", RedisConstant.COMMODITY_INFO_KEY_TTL_KEY, TimeUnit.MINUTES);
            return null;
        }
        //缓存到Redis中
        stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, JSONUtil.toJsonStr(result), RedisConstant.COMMODITY_INFO_KEY_TTL_KEY, TimeUnit.MINUTES);
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
                stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, "", RedisConstant.COMMODITY_INFO_KEY_TTL_KEY, TimeUnit.MINUTES);
                return null;
            }
            //缓存到Redis中
            stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, JSONUtil.toJsonStr(result), RedisConstant.COMMODITY_INFO_KEY_TTL_KEY, TimeUnit.MINUTES);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unLock(RedisConstant.COMMODITY_INFO_LOCK_KEY + id);
        }
        return null;
    }

    /**
     * 获取锁
     *
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
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
