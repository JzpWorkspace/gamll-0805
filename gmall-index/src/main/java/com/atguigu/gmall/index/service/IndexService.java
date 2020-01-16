package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
@Service
public class IndexService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private static final String KEY_PRIFIX="index:cates:";
    @Autowired
    private GmallPmsApi pmsApi;

    public List<CategoryEntity> queryLevel1Categories() {
        Resp<List<CategoryEntity>> categoriesResp = this.pmsApi.queryCategoryByLevelId(1, null);
        List<CategoryEntity> categoryEntities = categoriesResp.getData();
        return categoryEntities;
    }

    public List<CategoryVO> queryCategoriesWithSub(Long pid) {
        //获取缓存中的数据
        String cateJson = this.stringRedisTemplate.opsForValue().get(KEY_PRIFIX + pid);
        //有就返回
        if (StringUtils.isNotBlank(cateJson)){
            return JSON.parseArray(cateJson,CategoryVO.class);
        }
        //没有调用查询
        Resp<List<CategoryVO>> listResp = this.pmsApi.queryCategoriesWithSub(pid);
        List<CategoryVO> listVOS = listResp.getData();
        //反序列化一下放入缓存中
        this.stringRedisTemplate.opsForValue().set(KEY_PRIFIX+pid,JSON.toJSONString(listVOS));
        return listVOS;
    }
}
