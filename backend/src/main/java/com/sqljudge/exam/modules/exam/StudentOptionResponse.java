package com.sqljudge.exam.modules.exam;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentOptionResponse {
    private Long userId;
    private String username;
    private String realName;
}
