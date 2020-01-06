package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleVo {
    private Long skuId;
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
}
