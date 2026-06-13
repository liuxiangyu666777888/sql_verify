package com.sqljudge.exam.modules.admin;

import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String role;
}
