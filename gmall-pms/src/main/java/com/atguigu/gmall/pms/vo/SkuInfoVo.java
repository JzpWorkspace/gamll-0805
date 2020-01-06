package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoVo extends SkuInfoEntity {
    //营销积分信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;
    //满减打折优惠
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;
    //满减金额优惠
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
    //sale详情
    private  List<SkuSaleAttrValueEntity> saleAttrs;
    //图片字段
    private List<String> images;
}
