package com.sqljudge.exam.modules.question;

import lombok.Data;

@Data
public class QuestionRequest {
    private String title;
    private String description;
    private String difficulty;
    private String answerSql;
    private String sourceSchemaSql;
    private String tags;
    private Integer visible;
}
