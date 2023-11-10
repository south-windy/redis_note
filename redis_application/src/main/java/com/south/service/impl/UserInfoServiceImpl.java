package com.south.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.south.dto.UserDto;
import com.south.req.LoginForm;
import com.south.constant.RedisConstant;
import com.south.utils.AuthUserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.south.model.UserInfo;
import com.south.mapper.UserInfoMapper;
import com.south.service.UserInfoService;
import org.springframework.util.ObjectUtils;

/**
 * ${describe}
 *
 * @author 南风
 * @name UserInfoServiceImpl
 * @date 2023-07-04 11:17
 */
@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String sendLoginCode(String phone) {
        String code = RandomUtil.randomNumbers(5);
        log.info("手机号：{}，的验证码为：{}", phone, code);
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY + phone, "11111", RedisConstant.LOGIN_CODE_TTL_KEY, TimeUnit.MINUTES);
        return code;
    }

    @Override
    public String login(LoginForm loginForm) {
        String code = stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_CODE_KEY + loginForm.getPhone());
        if (ObjectUtils.isEmpty(code) || !code.equals(loginForm.getCode())) {
            log.error("验证码错误");
            return "验证码错误";
        }
        String uuid = IdUtil.randomUUID();
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getPhone, loginForm.getPhone()).eq(UserInfo::getDeleted, 0);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(userInfo)) {
            stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_USER_INFO_KEY + uuid, JSONUtil.toJsonStr(userInfo));
            log.info("登录成功：{}", uuid);
            return uuid;
        }
        userInfo = new UserInfo();
        userInfo.setUserName("用户名");
        userInfo.setPhone(loginForm.getPhone());
        userInfo.setCreateBy(0L);
        userInfo.setUpdateBy(0L);
        userInfo.setDeleted((byte) 0);
        int insert = userInfoMapper.insert(userInfo);
        UserDto userDto = new UserDto();
        userDto.setId(userInfo.getId());
        userDto.setUserName(userInfo.getUserName());
        userDto.setPhone(userInfo.getPhone());
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_USER_INFO_KEY + userDto.getId(), JSONUtil.toJsonStr(userDto), RedisConstant.LOGIN_USER_INFO_TTL_KEY, TimeUnit.MINUTES);
        log.info("注册成功：{}", insert);
        return uuid;
    }

    @Override
    public String loginOut() {
        UserDto userDto = AuthUserHolder.get();
        if (ObjectUtil.isEmpty(userDto)) {
            return "未获取到登录用户信息，请刷新重试！";
        }
        stringRedisTemplate.delete(RedisConstant.LOGIN_USER_INFO_KEY + userDto.getId());
        return "退出登录成功";
    }
}
