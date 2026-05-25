package com.sqljudge.exam.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String realName;
        private String role;
    }
}
