package com.sqljudge.exam.modules.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDetailResponse {
    private Long questionId;
    private String title;
    private String description;
    private String difficulty;
    private String sourceSchemaSql;
    private String tags;
    private String answerSql;
    private java.util.List<TestCaseRecord> testCases;
}
