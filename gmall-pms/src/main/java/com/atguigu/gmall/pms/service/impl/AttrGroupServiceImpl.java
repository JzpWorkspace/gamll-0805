package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCatId(QueryCondition queryCondition, Integer catId) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId));
        return new PageVo(page);
    }

    @Override
    public GroupVo getAttrByGid(Long gid) {
        GroupVo groupVo = new GroupVo();
        //根据gid查询组
        AttrGroupEntity attrGroupEntity = this.getById(gid);
        BeanUtils.copyProperties(attrGroupEntity,groupVo);
        //查询中间表
        List<AttrAttrgroupRelationEntity> relationEntities = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        groupVo.setRelations(relationEntities);

        //时刻注意空指针异常(在没有中间关系的情况下)
        if (CollectionUtils.isEmpty(relationEntities)){
            return groupVo;
        }
        //stream表达式将中间表中的groupId
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        //查询规格参数
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        groupVo.setAttrEntities(attrEntities);

        return groupVo;
    }

    @Override
    public List<GroupVo> getSpuInfoByCid(Long cid) {
        //1.查询attrEntities (List)
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));

        //2.遍历规格参数组，查询组下的关系。
        return  groupEntities.stream().map(attrGroupEntity -> this.getAttrByGid(attrGroupEntity.getAttrGroupId())).collect(Collectors.toList());
    }

    @Override
    public List<ItemGroupVO> queryItemGroupVoByCidAndSpuId(Long cid, Long spuId) {
        List<ItemGroupVO> itemGroupVOS = new ArrayList<>();
        //1.skuid查询spu的销售信息
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id",cid));
        if (CollectionUtils.isEmpty(groupEntities)){
            return null;
        }

        List<ItemGroupVO> collect = groupEntities.stream().map(group -> {
            ItemGroupVO itemGroupVO = new ItemGroupVO();
            itemGroupVO.setId(group.getAttrGroupId());
            itemGroupVO.setName(group.getAttrGroupName());
            //2.中间表查询每个组的规格参数Id
            List<AttrAttrgroupRelationEntity> relationEntities =  this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",group.getAttrGroupId()));
            if (!CollectionUtils.isEmpty(relationEntities)){
                List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                //3.查询name和值
               List<ProductAttrValueEntity> productAttrValueEntities = this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId).in("attr_id",attrIds));
                itemGroupVO.setBaseAttrValues(productAttrValueEntities);
            }
            return itemGroupVO;
        }).collect(Collectors.toList());
        return collect;
    }

}