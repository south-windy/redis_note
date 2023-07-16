package com.south.utils;

import com.south.dto.UserDto;

/**
 * @author 南风
 * @name AuthUserHolder
 * @date 2023-07-05 15:27
 */
public class AuthUserHolder {
    //维护ThreadLocal对象
    private static ThreadLocal<UserDto> threadLocal = new ThreadLocal<>();

    //放入User
    public static void set(UserDto userDto) {
        threadLocal.set(userDto);
    }

    //获取User
    public static UserDto get() {
        return threadLocal.get();
    }

    //移除User
    public static void remove() {
        threadLocal.remove();
    }
}
