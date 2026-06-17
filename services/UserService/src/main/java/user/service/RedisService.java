package user.service;

public interface RedisService {
    public void setToken(int id, String token, long expireTime);
    public String getToken(int id);
    public void deleteToken(int id);
    public boolean hasToken(int id);
}

