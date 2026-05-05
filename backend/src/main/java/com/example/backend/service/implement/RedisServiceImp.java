package com.example.backend.service.implement;

import com.example.backend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImp implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Lưu dữ liệu (có thời gian hết hạn)
    public void save(String key, Object value, long timeoutInMinutes) {
        redisTemplate.opsForValue().set(key, value, timeoutInMinutes, TimeUnit.MINUTES);
    }

    // Lấy dữ liệu
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Xóa dữ liệu
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // Kiểm tra key tồn tại
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
