package com.sqljudge.exam.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserResponse {
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private String role;
    private String status;
}
