package com.sqljudge.exam.modules.classes;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public ApiResponse<List<ClassRecord>> list() {
        return ApiResponse.ok(classService.listMine());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<ClassRecord> create(@RequestBody ClassRequest request) {
        return ApiResponse.ok(classService.create(request));
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<String> join(@RequestBody Map<String, String> request) {
        classService.join(request.get("inviteCode"));
        return ApiResponse.ok("ok");
    }
}
