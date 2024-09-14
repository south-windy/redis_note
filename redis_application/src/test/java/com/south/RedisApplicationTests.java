package com.south;

import com.south.service.impl.CommodityServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RedisApplicationTests {

    @Autowired
    CommodityServiceImpl commodityService;

    @Test
    void contextLoads() {
        commodityService.setCommodityInfoCache(1L,10L);
    }

}
