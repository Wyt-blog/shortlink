package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.constants.RedisCacheConstants;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    final RedissonClient redissonClient;

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
}
