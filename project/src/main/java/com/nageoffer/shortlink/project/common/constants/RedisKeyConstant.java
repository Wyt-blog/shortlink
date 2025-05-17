package com.nageoffer.shortlink.project.common.constants;

/**
 * redis key
 */
public interface RedisKeyConstant {

    /**
     * 跳转缓存 key
     */
    String GOTO_SHORT_LINK_KEY = "short-link_goto_%s";

    /**
     * 跳转缓存重构锁 key
     */
    String LOCK_GOTO_SHORT_LINK_KEY = "short-link_lock_goto_%s";

    /**
     * 不存在的连接跳转 key
     */
    String GO_IS_NULL_SHORT_LINK_KEY = "short-link_null_go_%s";

}
