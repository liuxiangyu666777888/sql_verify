package com.sqljudge.exam.modules.classes;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    @GetMapping
    public ApiResponse<Object> list() {
        return ApiResponse.ok(Collections.emptyList());
    }
}
