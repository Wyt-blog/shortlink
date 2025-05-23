package com.nageoffer.shortlink.admin.common.constants;

/**
 * 短连接后管 Redis 的 key 常量配置
 */
public interface RedisCacheConstants {

    String LOCK_USER_REGISTER_KEY = "short-link:lock-user-register:";

    String LOCK_GROUP_CREATE_KEY = "short-link:group-create:";

}
