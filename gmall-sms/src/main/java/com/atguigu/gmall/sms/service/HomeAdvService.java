package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.entity.HomeAdvEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 首页轮播广告
 *
 * @author jiaozepeng
 * @email jzp.workspace@foxmail.com
 * @date 2020-01-03 18:40:16
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageVo queryPage(QueryCondition params);
}

