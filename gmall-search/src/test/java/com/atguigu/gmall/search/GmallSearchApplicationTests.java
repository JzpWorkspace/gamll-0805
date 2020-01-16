package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareInfoEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GmallWmsClient wmsClient;

    @Test
    void creatIndex(){
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
    }

    @Test
    void importData(){
        Long pageNum =1l;
        Long pageSize =100l;

        do {
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            //分页查询
            Resp<List<SpuInfoEntity>> listResp = this.pmsClient.querySpuByPage(queryCondition);
            List<SpuInfoEntity> spuInfoEntities = listResp.getData();
            //判断是否为空
            if (CollectionUtils.isEmpty(spuInfoEntities)){
                return;
            }
            //遍历
            spuInfoEntities.forEach(spuInfoEntity -> {
                Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.getSkuInfoById(spuInfoEntity.getId());
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
                        Long brandId = spuInfoEntity.getBrandId();
                        Resp<BrandEntity> brandEntityResp = this.pmsClient.info(brandId);
                        if (brandEntityResp != null) {
                            goods.setBrandId(skuInfoEntity.getBrandId());
                            goods.setBrandName(brandEntityResp.getData().getName());
                        }
                        //获取组信息
                        Resp<CategoryEntity> categoryEntityResp = this.pmsClient.queryCategoryById(spuInfoEntity.getCatalogId());
                        CategoryEntity categoryEntity = categoryEntityResp.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getCatId());
                            goods.setCategoryName(categoryEntity.getName());
                        }
                        Long spuId = spuInfoEntity.getId();
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
            });
            pageSize=(long)spuInfoEntities.size();
        }while (pageSize==100);

    }


}
