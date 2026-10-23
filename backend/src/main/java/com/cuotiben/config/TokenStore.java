package com.cuotiben.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简令牌存储（内存版）。
 * 骨架阶段够用，正式版建议换成 JWT 或 Redis，接口不变即可。
 */
@Component
public class TokenStore {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final long expireHours;

    public TokenStore(@Value("${cuotiben.token-expire-hours:72}") long expireHours) {
        this.expireHours = expireHours;
    }

    public String issue(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(userId, LocalDateTime.now().plusHours(expireHours)));
        return token;
    }

    /**
     * 校验令牌，返回用户 id；无效或过期返回 null。
     */
    public Long resolve(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (session.expireAt().isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            return null;
        }
        return session.userId();
    }

    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private record Session(Long userId, LocalDateTime expireAt) {
    }
}
