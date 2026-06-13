package com.sqljudge.exam.modules.auth;

import com.sqljudge.exam.common.ApiResponse;
import com.sqljudge.exam.modules.auth.dto.AuthResponse;
import com.sqljudge.exam.modules.auth.dto.ChangePasswordRequest;
import com.sqljudge.exam.modules.auth.dto.LoginRequest;
import com.sqljudge.exam.modules.auth.dto.RegisterRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Validated @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse.UserInfo> me() {
        return ApiResponse.ok(authService.me());
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok("ok");
    }
}
