package com.sqljudge.exam.modules.exam;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public ApiResponse<List<ExamRecord>> list() {
        return ApiResponse.ok(examService.listMine());
    }

    @PostMapping
    public ApiResponse<ExamRecord> create(@RequestBody ExamRequest request) {
        return ApiResponse.ok(examService.create(request));
    }

    @GetMapping("/{examId}")
    public ApiResponse<ExamRecord> detail(@PathVariable Long examId) {
        return ApiResponse.ok(examService.detail(examId));
    }

    @GetMapping("/{examId}/questions")
    public ApiResponse<List<Map<String, Object>>> questions(@PathVariable Long examId) {
        return ApiResponse.ok(examService.questions(examId));
    }

    @GetMapping("/{examId}/scores")
    public ApiResponse<List<Map<String, Object>>> scores(@PathVariable Long examId) {
        return ApiResponse.ok(examService.scores(examId));
    }

    @PostMapping("/{examId}/publish")
    public ApiResponse<String> publish(@PathVariable Long examId) {
        examService.publish(examId);
        return ApiResponse.ok("ok");
    }

    @PostMapping("/{examId}/questions")
    public ApiResponse<String> addQuestions(@PathVariable Long examId, @RequestBody List<Map<String, Object>> questions) {
        examService.addQuestions(examId, questions);
        return ApiResponse.ok("ok");
    }

    @PostMapping("/{examId}/students")
    public ApiResponse<String> addStudents(@PathVariable Long examId, @RequestBody List<Long> studentIds) {
        examService.addStudents(examId, studentIds);
        return ApiResponse.ok("ok");
    }
}
