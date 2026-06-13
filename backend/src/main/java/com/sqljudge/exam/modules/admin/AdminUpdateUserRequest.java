package com.sqljudge.exam.modules.admin;

import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private String role;
    private String status;
}
