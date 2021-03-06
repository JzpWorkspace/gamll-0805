package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface GmallPmsApi {
    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpuByPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> getSkuInfoById(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> info(@PathVariable("brandId")Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId")Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> querySearchValue(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySpuById(@PathVariable("id")Long id);

    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategoryByLevelId(
            @RequestParam(value = "level",defaultValue = "0")Integer level,
            @RequestParam(value = "parentCid",required = false)Long parentId);

    @GetMapping("pms/category/{pid}")
    public Resp<List<CategoryVO>> queryCategoriesWithSub(@PathVariable("pid")Long pid);

    @ApiOperation("详情查询")
    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> querySkuById(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySpuInfoDescBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/withattrvalues")
    public Resp<List<ItemGroupVO>> queryItemGroupVoByCidAndSpuId(@RequestParam("cid")Long cid, @RequestParam("spuId")Long spuId);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrBySpuId(@PathVariable("spuId")Long spuId);
}
