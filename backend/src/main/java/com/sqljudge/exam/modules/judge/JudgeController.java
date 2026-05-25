package com.sqljudge.exam.modules.judge;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/judge")
public class JudgeController {
    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @PostMapping("/run")
    public ApiResponse<JudgeResult> run(@RequestBody JudgeRequest request) {
        return ApiResponse.ok(judgeService.run(request));
    }
}
