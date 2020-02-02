package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.config.ThreadPoolConfig;
import com.atguigu.gmall.item.dao.ItemDao;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVO queryItemVo(Long skuId) {
        ItemVO itemVO = new ItemVO();

        CompletableFuture<SkuInfoEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据skuid查询sku信息；
            Resp<SkuInfoEntity> skuInfoEntityResp = pmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            //查询sku信息
            if (skuInfoEntity == null) {
                return null;
            }
            //设置sku信息
            itemVO.setSkuId(skuId);
            itemVO.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVO.setSkuSubTitle(skuInfoEntity.getSkuSubtitle());
            itemVO.setPrice(skuInfoEntity.getPrice());
            itemVO.setWeight(skuInfoEntity.getWeight());
            return skuInfoEntity;
        },threadPoolExecutor);
        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //根据skucategoryid查询category信息；
            Long catalogId = skuInfoEntity.getCatalogId();
            CategoryEntity categoryEntityResp = this.pmsClient.queryCategoryById(catalogId).getData();
            if (categoryEntityResp != null) {
                //获取分组信息
                itemVO.setCategoryId(catalogId);
                itemVO.setCategoryName(categoryEntityResp.getName());
            }
        },threadPoolExecutor);
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //根据brandid查询brand信息；
            Long brandId = skuInfoEntity.getBrandId();
            BrandEntity brandEntity = this.pmsClient.info(brandId).getData();
            itemVO.setBrandId(brandId);
            itemVO.setBrandName(brandEntity.getName());
        },threadPoolExecutor);
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //根据skuid查询spu信息；
            SpuInfoEntity spuInfoEntityResp = this.pmsClient.querySpuById(skuInfoEntity.getSpuId()).getData();
            if (spuInfoEntityResp != null) {
                itemVO.setSpuId(spuInfoEntityResp.getId());
                itemVO.setSpuName(spuInfoEntityResp.getSpuName());
            }
        },threadPoolExecutor);
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            //查询sku的图片根据skuId
            List<SkuImagesEntity> imagesEntities = this.pmsClient.queryImagesBySkuId(skuId).getData();
            itemVO.setSkuImages(imagesEntities);
        },threadPoolExecutor);
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            //根据skuid查询库存信息
            Resp<List<WareSkuEntity>> listWareSkuResp = this.wmsClient.queryWareSkuBySpuId(skuId);
            List<WareSkuEntity> wareSkuEntities = listWareSkuResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
        },threadPoolExecutor);
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            //skuid查询
            List<ItemSaleVo> itemSaleVos = this.smsClient.queryItemSalesVoBySkuId(skuId).getData();
            itemVO.setSales(itemSaleVos);
        },threadPoolExecutor);
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            //根据skuId去sku表查询描述信息
            SpuInfoDescEntity spuInfoDescEntity = this.pmsClient.querySpuInfoDescBySpuId(skuInfoEntity.getSpuId()).getData();
            if (spuInfoDescEntity != null & StringUtils.isNotBlank(spuInfoDescEntity.getDecript())) {
                itemVO.setDesc(Arrays.asList(StringUtils.split(spuInfoDescEntity.getDecript(), ",")));
            }
        },threadPoolExecutor);
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            Resp<List<ItemGroupVO>> listResp = this.pmsClient.queryItemGroupVoByCidAndSpuId(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            if (listResp != null) {
                List<ItemGroupVO> data = listResp.getData();
                itemVO.setItemGroupVOS(data);
            }
        },threadPoolExecutor);
        CompletableFuture<Void> attrValuesCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            List<SkuSaleAttrValueEntity> data = this.pmsClient.querySkuSaleAttrBySpuId(skuInfoEntity.getSpuId()).getData();
            //1.先查询sku表中的所有sku（spu相同）
            //2.根据skuids查询销售属性
            itemVO.setSaleAttrValues(data);
        },threadPoolExecutor);
        CompletableFuture.allOf(categoryCompletableFuture,brandCompletableFuture,spuCompletableFuture,imageCompletableFuture,
                wareCompletableFuture,salesCompletableFuture,descCompletableFuture,groupCompletableFuture,attrValuesCompletableFuture).join();
        return itemVO;
    }
}
