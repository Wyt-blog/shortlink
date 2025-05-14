package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.constants.RedisCacheConstants;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    final RedissonClient redissonClient;

    final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        UserDO userDO = this.lambdaQuery().eq(UserDO::getUsername, username).one();
        UserRespDTO result = new UserRespDTO();
        if(userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParm) {
        if(!hasUsername(requestParm.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXITS);
        }
        // 根据用户名加锁 （防止大量用户同时注册一个用户名，导致大量数据打入数据库让索引进行兜底）
        RLock lock = redissonClient.getLock(RedisCacheConstants.LOCK_USER_REGISTER_KEY + requestParm.getUsername());
        try {
            if(lock.tryLock()){
                int inserted = baseMapper.insert(BeanUtil.copyProperties(requestParm, UserDO.class));
                if(inserted < 1) {
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParm.getUsername());
                return;
            }
            // 获取锁失败（不重试直接报错返回）
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXITS);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParm) {
        // TODO 验证当前登录用户是否为要修改的用户
        LambdaUpdateWrapper<UserDO> wrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername,requestParm.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParm,UserDO.class), wrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParm) {
        UserDO userDO = baseMapper.selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getUsername, requestParm.getUsername())
                .eq(UserDO::getPassword, requestParm.getPassword())
                .eq(UserDO::getDelFlag,0)
        );
        if(userDO == null) {
            throw new ClientException("用户名或密码错误");
        }
        String key = "login_" + requestParm.getUsername();
        Boolean hasLogin = stringRedisTemplate.hasKey(key);
        if(hasLogin != null && hasLogin) {
            throw new ClientException("用户已经登录");
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(key,uuid,JSON.toJSONString(userDO));
        stringRedisTemplate.expire(key,30,TimeUnit.DAYS);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username,String token) {
        return stringRedisTemplate.opsForHash().get("login_" + username,token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if(checkLogin(username,token)){
            stringRedisTemplate.delete("login_"+username);
            return;
        }
        throw new ClientException("用户未登录或用户Token不存在");
    }
}
