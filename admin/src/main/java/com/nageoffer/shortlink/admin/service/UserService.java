package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 返回用户实体类
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在啊
     *
     * @param username 要查询的用户名
     * @return 用户名是否存在（存在返回false，不存在返回true）
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     *
     * @param requestParm 用户注册实体类（用户名，密码，手机号......）
     */
    void register(UserRegisterReqDTO requestParm);

}
