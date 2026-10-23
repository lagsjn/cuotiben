package com.cuotiben.common;

/**
 * 当前登录用户上下文（由 AuthInterceptor 写入，请求结束时清理）。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long get() {
        return CURRENT_USER.get();
    }

    /**
     * 取当前用户 id，未登录直接抛 401 业务异常。
     */
    public static Long require() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw new BusinessException(40100, "登录已失效，请重新登录");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
