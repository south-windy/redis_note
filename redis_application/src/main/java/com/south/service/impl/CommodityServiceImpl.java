package com.south.service.impl;

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
        //todo 查询Redis中是否有缓存，有则返回
        String commodityInfo = stringRedisTemplate.opsForValue().get(RedisConstant.COMMODITY_INFO_KEY + id);
        if (StrUtil.isNotBlank(commodityInfo)) {
            CommodityInfo result = JSONUtil.toBean(commodityInfo, CommodityInfo.class);
            return result;
        }
        //todo 查询数据库，未查询到返回异常
        CommodityInfo result = commodityInfoMapper.selectByPrimaryKey(id);
        if (result == null) {
            return null;
        }

        //todo 缓存到Redis中
        stringRedisTemplate.opsForValue().set(RedisConstant.COMMODITY_INFO_KEY + id, JSONUtil.toJsonStr(result), RedisConstant.COMMODITY_INFO_KEY_TTL_KEY, TimeUnit.MINUTES);
        return result;
    }
}
