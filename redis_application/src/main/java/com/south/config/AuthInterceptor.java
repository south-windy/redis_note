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

/**
 * 用户信息拦截器
 *
 * @author 南风
 * @name AuthInterceptor
 * @date 2023-07-05 16:52
 */
public class AuthInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public AuthInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth_application = request.getHeader("auth_application");
        UserDto userDto = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_USER_INFO_KEY + auth_application), UserDto.class);
        if (ObjectUtil.isEmpty(userDto)){
            return false;
        }
        AuthUserHolder.set(userDto);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthUserHolder.remove();
    }
}
