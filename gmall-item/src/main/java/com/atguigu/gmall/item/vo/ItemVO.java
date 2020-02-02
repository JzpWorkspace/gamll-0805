package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data

public class ItemVO {
    private Long skuId;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long spuId;
    private String spuName;

    private String skuTitle;
    private String skuSubTitle;
    private BigDecimal price;
    private BigDecimal weight;
    private Boolean store;//库存信息

    private List<SkuImagesEntity> skuImages;//销售图片
    private List<ItemSaleVo> sales;//销售信息

    private List<SkuSaleAttrValueEntity> saleAttrValues;//spu下的所有sku销售组合

    private List<String> desc;//图片地址

    private List<ItemGroupVO> itemGroupVOS;

}
