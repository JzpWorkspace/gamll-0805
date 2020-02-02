package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IndexService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private GmallPmsApi pmsApi;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLevel1Categories() {
        Resp<List<CategoryEntity>> categoriesResp = this.pmsApi.queryCategoryByLevelId(1, null);
        List<CategoryEntity> categoryEntities = categoriesResp.getData();
        return categoryEntities;
    }

    @GmallCache(value = "index:cates:",timeout = 7200,bound = 100,lockName = "lock")
    public List<CategoryVO> queryCategoriesWithSub(Long pid) {
//        //获取缓存中的数据
//        String cateJson = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        //有就返回
//        if (StringUtils.isNotBlank(cateJson)) {
//            return JSON.parseArray(cateJson, CategoryVO.class);
//        }
//        //加入分布式锁
//        RLock lock = this.redissonClient.getLock("lock");
//        lock.lock();

//        //二次获取缓存中的数据
//        String cateJson1 = this.stringRedisTemplate.opsForValue().get(KEY_PRIFIX + pid);
//        //有就返回
//        if (StringUtils.isNotBlank(cateJson1)) {
//            //别忘记释放锁 不然会导致死锁状态
//            lock.unlock();
//            return JSON.parseArray(cateJson1, CategoryVO.class);
//        }

        //二次没有调用查询
        Resp<List<CategoryVO>> listResp = this.pmsApi.queryCategoriesWithSub(pid);
        List<CategoryVO> listVOS = listResp.getData();

//        反序列化一下放入缓存中
//        this.stringRedisTemplate.opsForValue().set(KEY_PRIFIX + pid, JSON.toJSONString(listVOS), 5 + new Random().nextInt(5), TimeUnit.DAYS);

//        lock.unlock();
        return listVOS;
    }
}
