package com.sqljudge.exam.modules.judge;

import lombok.Data;

@Data
public class SubmissionRecord {
    private Long submissionId;
    private Long userId;
    private Long questionId;
    private Long examId;
    private String sqlCode;
    private String status;
    private Double score;
    private Integer runtimeMs;
    private String errorMessage;
    private String resultPreview;
}
