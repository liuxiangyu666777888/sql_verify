package com.sqljudge.exam.modules.exam;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRequest {
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String instructions;
    private Boolean lockdownEnabled;
}
