package com.sqljudge.exam.modules.question;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        return ApiResponse.ok(questionService.page(keyword, page == null ? 1 : page, size == null ? 20 : size));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(questionService.detailResponse(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ASSISTANT', 'ADMIN')")
    public ApiResponse<QuestionRecord> create(@RequestBody QuestionRequest request) {
        return ApiResponse.ok(questionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ASSISTANT', 'ADMIN')")
    public ApiResponse<QuestionRecord> update(@PathVariable Long id, @RequestBody QuestionRequest request) {
        return ApiResponse.ok(questionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ASSISTANT', 'ADMIN')")
    public ApiResponse<String> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ApiResponse.ok("ok");
    }
}
