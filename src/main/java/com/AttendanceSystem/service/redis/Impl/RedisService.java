package com.AttendanceSystem.service.redis.Impl;

import com.AttendanceSystem.service.redis.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService implements IRedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 登录成功，存储token，用户名作为key，token作为value（把用户信息封装到token）
    public void setToken(String username, String token, long expireTime) {
        redisTemplate.opsForValue().set(username, token, expireTime, TimeUnit.SECONDS);
    }


    // 需要验证用户是否登录时，获取token
    public String getToken(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    // 退出时，删除token
    public void deleteToken(String username) {
        redisTemplate.delete(username);
    }

    // 调用get方法前，检查token是否存在
    public boolean hasToken(String username) {
        return redisTemplate.hasKey(username);
    }
}
