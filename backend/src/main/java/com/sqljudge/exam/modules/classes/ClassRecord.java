package com.sqljudge.exam.modules.classes;

import lombok.Data;

@Data
public class ClassRecord {
    private Long classId;
    private String className;
    private Long teacherId;
    private String semester;
    private String inviteCode;
}
