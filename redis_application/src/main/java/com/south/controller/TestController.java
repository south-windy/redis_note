package com.south.controller;

import com.south.dto.UserDto;
import com.south.utils.AuthUserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试类
 *
 * @author 南风
 * @name TestController
 * @date 2023-10-10 16:52
 */
@Slf4j
@RestController
@RequestMapping("/app/test")
public class TestController {

    @GetMapping("/getUser")
    public String loginOut() {
        UserDto userDto = AuthUserHolder.get();
        return userDto.getPhone();
    }
}
