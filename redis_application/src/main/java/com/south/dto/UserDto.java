package com.south.dto;

import lombok.Data;

/**
 * 用户信息
 *
 * @author 南风
 * @name UserDto
 * @date 2023-07-05 15:27
 */
@Data
public class UserDto {

    private String phone;

    private Long id;

    private String userName;
}
