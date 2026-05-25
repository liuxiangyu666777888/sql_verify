package com.sqljudge.exam.modules.question.dto;

import lombok.Data;

@Data
public class QuestionSummary {
    private Long questionId;
    private String title;
    private String difficulty;
    private String tags;
}
