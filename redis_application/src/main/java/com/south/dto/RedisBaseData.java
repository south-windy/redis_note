package com.south.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 南风
 * @name RedisBaseData
 * @date 2024-09-11 16:32
 */
@Data
public class RedisBaseData {

    private LocalDateTime expirationDate;

    private Object data;
}
