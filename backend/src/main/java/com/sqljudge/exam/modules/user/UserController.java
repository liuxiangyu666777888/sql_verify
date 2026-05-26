package com.sqljudge.exam.modules.user;

import com.sqljudge.exam.common.ApiResponse;
import com.sqljudge.exam.common.CurrentUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResponse<List<UserRecord>> list(@RequestParam(required = false) String role) {
        if (role != null) return ApiResponse.ok(userMapper.listByRole(role.toUpperCase()));
        return ApiResponse.ok(userMapper.listAll());
    }

    @GetMapping("/students")
    public ApiResponse<List<UserRecord>> listStudents() {
        return ApiResponse.ok(userMapper.listByRole("STUDENT"));
    }

    @PutMapping("/role")
    public ApiResponse<String> updateRole(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        String role = ((String) body.get("role")).toUpperCase();
        if (!List.of("STUDENT", "TEACHER", "ADMIN", "ASSISTANT").contains(role))
            return ApiResponse.fail(40000, "无效的角色");
        userMapper.updateRole(userId, role);
        return ApiResponse.ok("ok");
    }

    @PutMapping("/profile")
    public ApiResponse<String> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = body.containsKey("userId") ? ((Number) body.get("userId")).longValue() : CurrentUser.id();
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        UserRecord user = userMapper.findById(userId);
        if (user == null) return ApiResponse.fail(40400, "用户不存在");

        String newUsername = username != null ? username : user.getUsername();
        String newHash = password != null ? passwordEncoder.encode(password) : user.getPasswordHash();
        userMapper.updateProfile(userId, newUsername, newHash);
        return ApiResponse.ok("ok");
    }
}
