package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo getAttrByCidOrTypePage(QueryCondition queryCondition, Integer type, Long cid) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        if (type != null) {
            wrapper.eq("attr_type",type);
        }
        wrapper.eq("catelog_id", cid);
        IPage<AttrEntity> pageVo = this.page(
                new Query<AttrEntity>().getPage(queryCondition),
                wrapper
        );
        return new PageVo(pageVo);
    }

    @Override
    public void setAttrVo(AttrVo attrVo) {
        this.save(attrVo);
        Long attrId = attrVo.getAttrId();
        //新增
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrSort(0);
        relationEntity.setAttrId(attrId);
        relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        this.relationDao.insert(relationEntity);
    }

}