package com.sqljudge.exam.modules.user;

import lombok.Data;

@Data
public class UserRecord {
    private Long userId;
    private String username;
    private String passwordHash;
    private String realName;
    private String email;
    private String role;
    private String status;
}
