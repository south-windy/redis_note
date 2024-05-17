package com.south.mapper;

import com.south.model.CommodityInfo;
import org.apache.ibatis.annotations.Mapper;

/**
* @name CommodityInfoMapper
* @author 南风
* @date 2024-05-13 17:17
*/
@Mapper
public interface CommodityInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommodityInfo record);

    int insertSelective(CommodityInfo record);

    CommodityInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommodityInfo record);

    int updateByPrimaryKey(CommodityInfo record);
}