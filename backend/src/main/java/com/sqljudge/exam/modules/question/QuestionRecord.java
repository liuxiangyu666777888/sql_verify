package com.sqljudge.exam.modules.question;

import lombok.Data;

@Data
public class QuestionRecord {
    private Long questionId;
    private String title;
    private String description;
    private String difficulty;
    private String answerSql;
    private Long creatorId;
    private String sourceSchemaSql;
    private String tags;
    private Integer visible;
}
