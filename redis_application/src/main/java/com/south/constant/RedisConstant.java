package com.south.constant;

/**
 * @author 南风
 * @name RedisConstant
 * @date 2023-07-04 17:21
 */
public class RedisConstant {
    /**
     * 登录验证码
     */
    public static final String LOGIN_CODE_KEY = "user:login:code:";

    /**
     * 登录验证码过期时间 单位：分
     */
    public static final Long LOGIN_CODE_TTL_KEY = 5L;

    /**
     * 已登录用户信息缓存
     */
    public static final String LOGIN_USER_INFO_KEY = "login:user:info:";

    /**
     * 已登录用户信息缓存时间
     */
    public static final Long LOGIN_USER_INFO_TTL_KEY = 30L;
}
