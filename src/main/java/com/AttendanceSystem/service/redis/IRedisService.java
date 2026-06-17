package com.AttendanceSystem.service.redis;

public interface IRedisService {
    public void setToken(String username, String token, long expireTime);
    public String getToken(String username);
    public void deleteToken(String username);
    public boolean hasToken(String username);
}