package com.sqljudge.exam.modules.assistant;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantStudentService service;

    public AssistantController(AssistantStudentService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public ApiResponse<List<AssistantStudentRecord>> listByAssistant(@RequestParam Long assistantId) {
        return ApiResponse.ok(service.listByAssistant(assistantId));
    }

    @PostMapping("/assign")
    public ApiResponse<String> assign(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("studentIds");
        Object aidObj = body.get("assistantId");
        if (rawIds == null || aidObj == null) return ApiResponse.fail(40000, "参数不全");

        List<Long> studentIds = rawIds.stream().map(Integer::longValue).toList();
        Long assistantId = ((Number) aidObj).longValue();
        service.assign(studentIds, assistantId);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping("/unbind")
    public ApiResponse<String> unbind(@RequestParam Long assistantId, @RequestParam Long studentId) {
        service.remove(assistantId, studentId);
        return ApiResponse.ok("ok");
    }
}
