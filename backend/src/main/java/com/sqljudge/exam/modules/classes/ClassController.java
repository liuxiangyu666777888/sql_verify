package com.sqljudge.exam.modules.classes;

import com.sqljudge.exam.common.ApiResponse;
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

    @PostMapping("/join")
    public ApiResponse<String> join(@RequestBody Map<String, String> request) {
        return ApiResponse.ok("ok");
    }
}
