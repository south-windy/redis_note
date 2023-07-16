package com.south.service;

import com.south.req.LoginForm;
import com.south.model.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * ${describe}
 *
 * @author 南风
 * @name UserInfoService
 * @date 2023-07-04 11:17
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 登录验证码
     * @return
     */
    String sendLoginCode(String phone);

    String login(LoginForm loginForm);

    String loginOut();

}
