package com.sqljudge.exam.modules.question;

import lombok.Data;

@Data
public class TestCaseRecord {
    private Long caseId;
    private Long questionId;
    private String inputSql;
    private String expectedOutput;
    private Integer caseOrder;
    private Integer isHidden;
}
