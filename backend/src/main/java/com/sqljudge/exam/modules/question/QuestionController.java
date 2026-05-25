package com.sqljudge.exam.modules.question;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ApiResponse<List<QuestionRecord>> list() {
        return ApiResponse.ok(questionService.list(null));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionRecord> detail(@PathVariable Long id) {
        return ApiResponse.ok(questionService.detail(id));
    }
}
