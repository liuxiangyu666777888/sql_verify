package com.sqljudge.exam.modules.admin;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<List<AdminUserResponse>> list() {
        return ApiResponse.ok(adminUserService.list());
    }

    @PostMapping
    public ApiResponse<AdminUserResponse> create(@RequestBody AdminCreateUserRequest request) {
        return ApiResponse.ok(adminUserService.create(request));
    }

    @PutMapping("/{userId}")
    public ApiResponse<AdminUserResponse> update(@PathVariable Long userId, @RequestBody AdminUpdateUserRequest request) {
        return ApiResponse.ok(adminUserService.update(userId, request));
    }
}
