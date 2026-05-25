package com.sqljudge.exam.modules.judge;

import lombok.Data;

@Data
public class JudgeRequest {
    private Long questionId;
    private Long examId;
    private String sqlCode;
}
