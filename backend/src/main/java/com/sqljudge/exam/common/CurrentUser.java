package com.sqljudge.exam.common;

import com.sqljudge.exam.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static UserPrincipal get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw BusinessException.unauthorized("未登录");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public static Long id() {
        return get().getUserId();
    }

    public static String role() {
        return get().getRole();
    }
}
