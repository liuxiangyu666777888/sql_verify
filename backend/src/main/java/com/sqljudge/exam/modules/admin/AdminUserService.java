package com.sqljudge.exam.modules.admin;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.modules.user.UserMapper;
import com.sqljudge.exam.modules.user.UserRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserResponse> list() {
        return userMapper.listAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AdminUserResponse create(AdminCreateUserRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw BusinessException.badRequest("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw BusinessException.badRequest("密码至少需要 6 位");
        }
        if (userMapper.findByUsername(request.getUsername().trim()) != null) {
            throw BusinessException.badRequest("用户名已存在");
        }
        UserRecord record = new UserRecord();
        record.setUsername(request.getUsername().trim());
        record.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        record.setRealName(request.getRealName());
        record.setEmail(request.getEmail());
        record.setRole(normalizeRole(request.getRole()));
        userMapper.insert(record);
        return toResponse(userMapper.findByUsername(record.getUsername()));
    }

    public AdminUserResponse update(Long userId, AdminUpdateUserRequest request) {
        UserRecord existing = userMapper.findById(userId);
        if (existing == null) {
            throw BusinessException.notFound("用户不存在");
        }
        String role = normalizeRole(request == null ? existing.getRole() : request.getRole());
        String status = normalizeStatus(request == null ? existing.getStatus() : request.getStatus());
        userMapper.updateRoleAndStatus(userId, role, status);
        return toResponse(userMapper.findById(userId));
    }

    private AdminUserResponse toResponse(UserRecord record) {
        return new AdminUserResponse(
                record.getUserId(),
                record.getUsername(),
                record.getRealName(),
                record.getEmail(),
                record.getRole(),
                record.getStatus()
        );
    }

    private String normalizeRole(String role) {
        String value = role == null || role.trim().isEmpty() ? "STUDENT" : role.trim().toUpperCase();
        if (!"STUDENT".equals(value) && !"TEACHER".equals(value) && !"ADMIN".equals(value) && !"ASSISTANT".equals(value)) {
            throw BusinessException.badRequest("用户角色不合法");
        }
        return value;
    }

    private String normalizeStatus(String status) {
        String value = status == null || status.trim().isEmpty() ? "ACTIVE" : status.trim().toUpperCase();
        if (!"ACTIVE".equals(value) && !"DISABLED".equals(value)) {
            throw BusinessException.badRequest("用户状态不合法");
        }
        return value;
    }
}
