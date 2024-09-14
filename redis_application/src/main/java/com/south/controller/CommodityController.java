package com.south.controller;

import com.south.model.CommodityInfo;
import com.south.service.CommodityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 商品
 *
 * @author 南风
 * @name CommodityController
 * @date 2024-05-13 17:20
 */
@Slf4j
@RestController
@RequestMapping("/app/commodity")
public class CommodityController {

    @Resource
    private CommodityService commodityService;

    @GetMapping("/getCommodityInfo")
    public CommodityInfo getCommodityInfo(@RequestParam("id") Long id) {
        return commodityService.getCommodityInfo(id);
    }
}
