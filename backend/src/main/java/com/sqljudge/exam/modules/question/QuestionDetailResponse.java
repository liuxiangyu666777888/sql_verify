package com.sqljudge.exam.modules.question;

import lombok.Data;

@Data
public class QuestionDetailResponse {
    private Long questionId;
    private String title;
    private String description;
    private String difficulty;
    private String sourceSchemaSql;
    private String tags;
}
