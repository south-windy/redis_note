package com.south.redis.entity;

import lombok.Data;

/**
 * @author 南风
 * @name User
 * @date 2023-06-01 17:43
 */
@Data
public class User {

    private String name;

    private int age;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
