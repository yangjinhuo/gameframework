package com.scriptplatform.security;

/**
 * Thread-local login user holder.
 */
public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static String username() {
        LoginUser u = HOLDER.get();
        return u == null ? "anonymous" : u.getUsername();
    }

    public static String role() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getRole();
    }
}
