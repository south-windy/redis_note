package com.south;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.south.redis.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class SpringdataRedisDemoApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void redisTemplateTest() {
        redisTemplate.opsForValue().set("testKey1", "张三");
        System.out.println(redisTemplate.opsForValue().get("testKey1"));

        User user = new User();
        user.setName("李四");
        user.setAge(100);
        redisTemplate.opsForValue().set("user", user);
        System.out.println(redisTemplate.opsForValue().get("user").toString());
    }

    @Test
    void stringRedisTest() throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set("testKey2", "张三");
        System.out.println(stringRedisTemplate.opsForValue().get("testKey1"));

        User user = new User();
        user.setName("李四");
        user.setAge(100);
        stringRedisTemplate.opsForValue().set("user1", objectMapper.writeValueAsString(user));
        System.out.println(objectMapper.readValue(stringRedisTemplate.opsForValue().get("user1"), User.class));

    }

    @Test
    void testHash() {
        stringRedisTemplate.opsForHash().put("user:zs", "name", "张三");
        stringRedisTemplate.opsForHash().put("user:zs", "age", "18");
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries("user:zs");
        System.out.println("值为：" + entries);
    }

}
