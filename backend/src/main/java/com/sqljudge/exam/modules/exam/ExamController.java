package com.sqljudge.exam.modules.exam;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    @GetMapping
    public ApiResponse<Object> list() {
        return ApiResponse.ok(Collections.emptyList());
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(Collections.singletonMap("message", "考试草稿接口已预留，后续可接入exams表写入"));
    }
}
