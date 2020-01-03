package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author jiaozepeng
 * @email lxf@atguigu.com
 * @date 2020-01-02 16:36:26
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
