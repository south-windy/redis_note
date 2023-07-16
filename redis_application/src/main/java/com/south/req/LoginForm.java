package com.south.req;

import lombok.Data;

/**
 * 登录请求参数
 *
 * @author 南风
 * @name LoginForm
 * @date 2023-07-04 17:30
 */
@Data
public class LoginForm {

    /**
     * 手机
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;
}
