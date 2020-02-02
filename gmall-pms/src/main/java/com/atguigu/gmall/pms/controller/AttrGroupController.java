package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;




/**
 * 属性分组
 *
 * @author jiaozepeng
 * @email lxf@atguigu.com
 * @date 2020-01-02 16:36:26
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("withattrvalues")
    public Resp<List<ItemGroupVO>> queryItemGroupVoByCidAndSpuId(
            @RequestParam("cid")Long cid,
            @RequestParam("spuId")Long spuId
    ){
        List<ItemGroupVO> itemGroupVOS = attrGroupService.queryItemGroupVoByCidAndSpuId(cid,spuId);
        return Resp.ok(itemGroupVOS);
    }


    //Request URL: http://127.0.0.1:8888/pms/attrgroup/withattrs/cat/225
    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<GroupVo>> getSpuInfoByCid(@PathVariable("catId")Long cid){
        List<GroupVo> groupVoList = attrGroupService.getSpuInfoByCid(cid);
        return Resp.ok(groupVoList);
    }


    @GetMapping("withattr/{gid}")
    public Resp<GroupVo> getAttrByGid(@PathVariable("gid")Long gid){
        GroupVo groupVo = attrGroupService.getAttrByGid(gid);
        return Resp.ok(groupVo);
    }

    @GetMapping("{catId}")
    public Resp<PageVo> queryByCatId(@PathVariable("catId")Integer catId,QueryCondition queryCondition){
        PageVo pageVo = attrGroupService.queryByCatId(queryCondition,catId);
        return Resp.ok(pageVo);
    }
    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
