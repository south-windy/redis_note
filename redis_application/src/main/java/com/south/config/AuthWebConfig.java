package com.south.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * APP拦截器
 *
 * @author 南风
 * @name AuthWebConfig
 * @date 2023-07-05 16:54
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 配置拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate));
        //添加拦截器
        registry.addInterceptor(new LoginInterceptor())
                //配置拦截路径 addPathPatterns("/**")表示拦截所有请求，包括我们的静态资源
                .excludePathPatterns("/", "/error", "/app/user/sendLoginCode", "/app/user/login").addPathPatterns("/**")
        //如果有静态资源的时候可以在这个地方放行
        ;
    }
}
