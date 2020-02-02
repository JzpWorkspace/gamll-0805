package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品sku积分设置
 *
 * @author jiaozepeng
 * @email jzp.workspace@foxmail.com
 * @date 2020-01-03 18:40:16
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

    void saveSales(SaleVo saleVo);

    List<ItemSaleVo> queryItemSalesVoBySkuId(Long skuId);
}

