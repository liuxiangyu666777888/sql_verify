package com.sqljudge.exam.modules.judge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResult {
    private String status;
    private double score;
    private Integer runtimeMs;
    private String errorMessage;
    private Map<String, Object> resultPreview;
    private List<Map<String, Object>> details;
}
