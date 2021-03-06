package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrValueVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private ProductAttrValueService productAttrValueDaoService;
    @Autowired
    private  SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo getSpuInfoByCatId(QueryCondition queryCondition, Long catId) {
        //封装分页条件和查询条件
        IPage<SpuInfoEntity> page = new Query<SpuInfoEntity>().getPage(queryCondition);
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<SpuInfoEntity>();
        //判断catId条件是否为空则不查找
        if (catId != null) {
            wrapper.eq("catalog_id", catId);
        }
        String key = queryCondition.getKey();
        if (StringUtils.isNotEmpty(key)){
            wrapper.and(t->t.like("id",key)).or().like("spu_name",key);
        }
        IPage<SpuInfoEntity> spuInfoEntityIPage = this.page(page,wrapper);
        return new PageVo(spuInfoEntityIPage);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuInfoVo spuInfoVo) {
        //1.保存spu相关信息
        //1.1. spuInfo 基本信息
        Long spuId = saveSpuInfo(spuInfoVo);
        //1.2. spuInfoDesc 描述
        this.spuInfoDescService.saveSpuInfoDesc(spuInfoVo, spuId);
        //1.3.baseAttr 基础属性相关信息
        saveBaseAttrValue(spuInfoVo, spuId);
        //2.sku相关信息
        saveSkuAndSales(spuInfoVo, spuId);
        //加入异常。
        //int i =1/0;
        sendMsg(spuId,"insert");
    }

    private void sendMsg(Long spuId,String type){
        this.amqpTemplate.convertAndSend("GMALL-PMS-EXCHANGE","item."+type,spuId);
    }

    private void saveSkuAndSales(SpuInfoVo spuInfoVo, Long spuId) {
        List<SkuInfoVo> skus = spuInfoVo.getSkus();
        //为空就结束
        if (CollectionUtils.isEmpty(skus)){return;}
        //2.1. skuInfo 属性信息

        skus.forEach(sku->{
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku,skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)){
                //使代码更长久使用的办法 注意体现工作经验的代码。
                skuInfoEntity.setSkuDefaultImg(skuInfoEntity.getSkuDefaultImg()==null? images.get(0):skuInfoEntity.getSkuDefaultImg());
            }
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoEntity.setCatalogId(spuInfoVo.getCatalogId());
            skuInfoEntity.setBrandId(spuInfoVo.getBrandId());
            this.skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();
            //2.2. skuInfoImage 图片信息
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> saveImages = images.stream().map(
                        image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setImgSort(0);
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(image);
                            skuImagesEntity.setDefaultImg(StringUtils.equals(image, skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                            return skuImagesEntity;
                        }
                ).collect(Collectors.toList());
                this.skuImagesService.saveBatch(saveImages);
            }
            //2.3. skuSaleAttrValue 销售属性
            List<SkuSaleAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttrValueEntity->{
                    saleAttrValueEntity.setSkuId(skuId);
                    saleAttrValueEntity.setAttrSort(0);
                });
                skuSaleAttrValueService.saveBatch(saleAttrs);
            }
            //3.营销相关信息 feign调用sms营销微服务保存。
            SaleVo saleVo = new SaleVo();
            BeanUtils.copyProperties(sku,saleVo);
            saleVo.setSkuId(skuId);
            this.gmallSmsClient.saveSales(saleVo);
        });
    }

    private void saveBaseAttrValue(SpuInfoVo spuInfoVo, Long spuId) {
        List<BaseAttrValueVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> attrValues = baseAttrs.stream().map(baseAttrValueVo -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                BeanUtils.copyProperties(baseAttrValueVo, productAttrValueEntity);
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setAttrSort(0);
                productAttrValueEntity.setQuickShow(0);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            this.productAttrValueDaoService.saveBatch(attrValues);
        }
    }

    private Long saveSpuInfo(SpuInfoVo spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }

}