package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PmsListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL-SEARCH-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId){
            Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.getSkuInfoById(spuId);
            List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
            if (!CollectionUtils.isEmpty(skuInfoEntities)){
                List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                    Goods goods = new Goods();
                    //查询库存信息
                    Resp<List<WareSkuEntity>> wareSkuResp = this.wmsClient.queryWareSkuBySpuId(skuInfoEntity.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)){
                        goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()>0));
                    }
                    //获取品牌信息
                    SpuInfoEntity spuInfoEntity = this.pmsClient.querySpuById(spuId).getData();
                    Long brandId = spuInfoEntity.getBrandId();
                    Resp<BrandEntity> brandEntityResp = this.pmsClient.info(brandId);
                    if (brandEntityResp != null) {
                        goods.setBrandId(skuInfoEntity.getBrandId());
                        goods.setBrandName(brandEntityResp.getData().getName());
                    }
                    //获取组信息
                    Resp<CategoryEntity> categoryEntityResp = this.pmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                    CategoryEntity categoryEntity = categoryEntityResp.getData();
                    if (categoryEntity != null) {
                        goods.setCategoryId(categoryEntity.getCatId());
                        goods.setCategoryName(categoryEntity.getName());
                    }
                    Resp<List<ProductAttrValueEntity>> searchValueList = this.pmsClient.querySearchValue(spuId);
                    List<ProductAttrValueEntity> productAttrValueEntities = searchValueList.getData();
                    if (!CollectionUtils.isEmpty(productAttrValueEntities)){
                        List<SearchAttrValue> searchAttrValues = productAttrValueEntities.stream().map(productAttrValueEntity ->
                        {
                            SearchAttrValue searchAttrValue = new SearchAttrValue();
                            searchAttrValue.setAttrId(productAttrValueEntity.getAttrId());
                            searchAttrValue.setAttrName(productAttrValueEntity.getAttrName());
                            searchAttrValue.setAttrValue(productAttrValueEntity.getAttrValue());
                            return searchAttrValue;
                        }).collect(Collectors.toList());
                        goods.setAttrs(searchAttrValues);
                    }
                    goods.setSkuId(skuInfoEntity.getSkuId());
                    goods.setSale(10l);
                    goods.setPrice(skuInfoEntity.getPrice().doubleValue());
                    goods.setCreatTime(spuInfoEntity.getCreateTime());
                    goods.setCategoryId(skuInfoEntity.getCatalogId());

                    goods.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                    goods.setSkuSubTitle(skuInfoEntity.getSkuSubtitle());
                    goods.setSkuTitle(skuInfoEntity.getSkuTitle());
                    return goods;
                }).collect(Collectors.toList());
                this.goodsRepository.saveAll(goodsList);
            }
    }

}
