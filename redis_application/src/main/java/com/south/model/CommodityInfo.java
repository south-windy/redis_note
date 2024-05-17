package com.south.model;

/**
 * @author 南风
 * @name CommodityInfo
 * @date 2024-05-13 17:17
 */
public class CommodityInfo {
    /**
     * id
     */
    private Long id;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品图片
     */
    private String commodityImage;

    /**
     * 商品价格（单位：分）
     */
    private Long commodityPrice;

    /**
     * sku编码
     */
    private String commoditySkuCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public String getCommodityImage() {
        return commodityImage;
    }

    public void setCommodityImage(String commodityImage) {
        this.commodityImage = commodityImage;
    }

    public Long getCommodityPrice() {
        return commodityPrice;
    }

    public void setCommodityPrice(Long commodityPrice) {
        this.commodityPrice = commodityPrice;
    }

    public String getCommoditySkuCode() {
        return commoditySkuCode;
    }

    public void setCommoditySkuCode(String commoditySkuCode) {
        this.commoditySkuCode = commoditySkuCode;
    }
}