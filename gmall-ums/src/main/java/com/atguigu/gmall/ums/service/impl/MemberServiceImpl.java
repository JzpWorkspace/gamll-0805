package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.UmsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;



@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    public StringRedisTemplate redisTemplate;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        switch (type){
            case 1: queryWrapper.eq("username",data); break;
            case 2: queryWrapper.eq("mobile",data); break;
            case 3: queryWrapper.eq("email",data); break;
            default: return null;
        }
        return this.count(queryWrapper)==0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        //1.校验验证码
        String redisCode = redisTemplate.opsForValue().get(memberEntity.getMobile());
        if (!StringUtils.equals(redisCode,code)){
            throw new UmsException("用户验证码错误或已失效！");
        }
        //2.生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        memberEntity.setSalt(salt);
        //3.加盐加密
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword()+salt));
        //4.保存用户信息
        memberEntity.setLevelId(1l);
        memberEntity.setSourceType(1);
        memberEntity.setGrowth(2000);
        memberEntity.setIntegration(1000);
        memberEntity.setStatus(1);
        memberEntity.setCreateTime(new Date());
        this.save(memberEntity);
        //5.删除验证码
        this.redisTemplate.delete(memberEntity.getMobile());
    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        //1.根据用户名获取用户盐信息
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username",username));
        if (memberEntity == null) {
            return null;
        }
        //2.密码加盐
        password = DigestUtils.md5Hex(password+ memberEntity.getSalt());
        String password2 = memberEntity.getPassword();
        //3.对比密码是否正确
        if (!StringUtils.equals(password,password2)){
            return null;
        }

        return memberEntity;
    }

}