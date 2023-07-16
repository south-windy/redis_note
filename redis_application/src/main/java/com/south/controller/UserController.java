package com.south.controller;

import com.south.req.LoginForm;
import com.south.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户接口
 *
 * @author 南风
 * @name UserController
 * @date 2023-07-04 11:18
 */
@Slf4j
@RestController
@RequestMapping("/app/user")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 发送登录验证码
     *
     * @param phone
     * @return
     */
    @GetMapping("/sendLoginCode")
    public String sendLoginCode(@RequestParam("phone") String phone) {
        return userInfoService.sendLoginCode(phone);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginForm loginForm) {
        return userInfoService.login(loginForm);
    }

    @GetMapping("/loginOut")
    public String loginOut() {
        return userInfoService.loginOut();
    }
}
