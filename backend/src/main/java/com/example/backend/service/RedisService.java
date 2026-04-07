package com.example.backend.service;

public interface RedisService {
    void save(String key, Object value, long timeoutInMinutes);
    Object get(String key);
    void delete(String key);
    boolean hasKey(String key);
}
