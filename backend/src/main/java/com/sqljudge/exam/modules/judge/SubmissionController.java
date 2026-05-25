package com.sqljudge.exam.modules.judge;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ApiResponse<JudgeResult> submit(@RequestBody JudgeRequest request) {
        return ApiResponse.ok(submissionService.submit(request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Map<String, Object>>> mine() {
        return ApiResponse.ok(submissionService.mine());
    }
}
