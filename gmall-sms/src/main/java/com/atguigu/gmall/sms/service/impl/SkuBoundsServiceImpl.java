package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private  SkuBoundsDao skuBoundsDao;
    @Autowired
    private SkuLadderDao skuLadderDao;
    @Autowired
    private SkuFullReductionDao skuFullReductionDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSales(SaleVo saleVo) {
        //3.营销相关信息
        //3.1. skuBounds 积分
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleVo,skuBoundsEntity);
        skuBoundsEntity.setSkuId(saleVo.getSkuId());
        List<Integer> work = saleVo.getWork();
        skuBoundsEntity.setWork(new Integer(work.get(0))+new Integer(work.get(1))*2+new Integer(work.get(2))*4+new Integer(work.get(3))*8);
        this.skuBoundsDao.insert(skuBoundsEntity);
        //3.2. skuLadder 打折
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleVo,skuLadderEntity);
        skuLadderEntity.setSkuId(saleVo.getSkuId());
        skuLadderEntity.setAddOther(saleVo.getLadderAddOther());
        this.skuLadderDao.insert(skuLadderEntity);
        //3.3. FullReduction 满减
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(saleVo,skuFullReductionEntity);
        skuFullReductionEntity.setSkuId(saleVo.getSkuId());
        skuFullReductionEntity.setAddOther(saleVo.getFullAddOther());
        this.skuFullReductionDao.insert(skuFullReductionEntity);
    }

    @Override
    public List<ItemSaleVo> queryItemSalesVoBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos = new ArrayList<>();
        //1.积分
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id",skuId));
        if (skuBoundsEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVos.add(itemSaleVo);
            itemSaleVo.setType("积分");
            itemSaleVo.setDesc("共赠送"+skuBoundsEntity.getBuyBounds()+"购物积分，"+skuBoundsEntity.getGrowBounds()+"成长积分。");
        }
        //2.满减
        SkuFullReductionEntity reductionEntity = this.skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id",skuId));
        if (reductionEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVos.add(itemSaleVo);
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满"+reductionEntity.getFullPrice()+"元减"+reductionEntity.getReducePrice()+"元。");
        }
        //3.打折
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id",skuId));
        if (skuLadderEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVos.add(itemSaleVo);
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满"+skuLadderEntity.getFullCount()+"件，打"+skuLadderEntity.getDiscount().divide(new BigDecimal(10))+"折。");
        }
        return itemSaleVos;
    }

}