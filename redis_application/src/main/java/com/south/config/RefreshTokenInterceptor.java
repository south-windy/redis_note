package com.south.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.south.constant.RedisConstant;
import com.south.dto.UserDto;
import com.south.utils.AuthUserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 拦截所有请求
 * 有token则刷新token并获取用户信息放置到threadLocal
 * 没有则放行
 *
 * @author 南风
 * @name AuthInterceptor
 * @date 2023-07-05 16:52
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth_application = request.getHeader("auth_application");
        if (ObjectUtil.isEmpty(auth_application)) {
            return true;
        }
        String userDtoStr = stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_USER_INFO_KEY + auth_application);
        if (ObjectUtil.isEmpty(userDtoStr)) {
            return true;
        }
        UserDto userDto = JSONUtil.toBean(userDtoStr, UserDto.class);
        AuthUserHolder.set(userDto);
        //刷新过期时间
        stringRedisTemplate.expire(RedisConstant.LOGIN_USER_INFO_KEY + auth_application, RedisConstant.LOGIN_USER_INFO_TTL_KEY, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthUserHolder.remove();
    }
}
