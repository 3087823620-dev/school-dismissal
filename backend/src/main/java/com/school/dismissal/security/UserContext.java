package com.school.dismissal.security;

/**
 * 当前登录用户上下文（ThreadLocal）
 *
 * 每个请求经过 JwtAuthInterceptor 时把登录信息塞进来，
 * Controller/Service 随时能拿到当前是谁在操作，请求结束时 clear。
 */
public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) { HOLDER.set(user); }
    public static LoginUser get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    /** 当前登录用户 ID */
    public static Integer currentUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getId();
    }

    /** 当前登录用户角色：admin / teacher */
    public static String currentRole() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getRole();
    }

    /** 是否是管理员（管理员看所有数据） */
    public static boolean isAdmin() {
        return "admin".equals(currentRole());
    }
}
