package user.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import user.service.RedisService;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 登录成功，存储token，用户ID作为key，token作为value（把用户信息封装到token）
    public void setToken(int userId, String token, long expireTime) {
        redisTemplate.opsForValue().set(String.valueOf(userId), token, expireTime, TimeUnit.SECONDS);
    }


    // 需要验证用户是否登录时，获取token
    public String getToken(int userId) {
        return redisTemplate.opsForValue().get(String.valueOf(userId));
    }

    // 退出时，删除token
    public void deleteToken(int userId) {
        redisTemplate.delete(String.valueOf(userId));
    }

    // 调用get方法前，检查token是否存在
    public boolean hasToken(int userId) {
        return redisTemplate.hasKey(String.valueOf(userId));
    }
}
