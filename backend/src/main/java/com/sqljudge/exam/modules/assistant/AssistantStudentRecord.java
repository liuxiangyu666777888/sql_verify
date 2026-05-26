package com.sqljudge.exam.modules.assistant;

import lombok.Data;

@Data
public class AssistantStudentRecord {
    private Long assistantId;
    private Long studentId;
    // joined
    private String studentName;
    private String assistantName;
}
