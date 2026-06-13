package com.sqljudge.exam.modules.auth;

import com.sqljudge.exam.common.BusinessException;
import com.sqljudge.exam.common.CurrentUser;
import com.sqljudge.exam.modules.auth.dto.AuthResponse;
import com.sqljudge.exam.modules.auth.dto.ChangePasswordRequest;
import com.sqljudge.exam.modules.auth.dto.LoginRequest;
import com.sqljudge.exam.modules.auth.dto.RegisterRequest;
import com.sqljudge.exam.modules.user.UserMapper;
import com.sqljudge.exam.modules.user.UserRecord;
import com.sqljudge.exam.security.JwtTokenProvider;
import com.sqljudge.exam.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthService(JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public AuthResponse login(LoginRequest request) {
        UserRecord record = userMapper.findByUsername(request.getUsername());
        if (record == null) {
            throw BusinessException.unauthorized("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), record.getPasswordHash())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        UserPrincipal principal = new UserPrincipal(record.getUserId(), record.getUsername(), record.getPasswordHash(), record.getRole(), "ACTIVE".equals(record.getStatus()));
        String token = jwtTokenProvider.generateToken(principal);
        return new AuthResponse(token, new AuthResponse.UserInfo(record.getUserId(), record.getUsername(), record.getRealName(), record.getRole()));
    }

    public AuthResponse register(RegisterRequest request) {
        throw BusinessException.forbidden("第一版不开放公开注册，请使用初始化账号");
    }

    public AuthResponse.UserInfo me() {
        UserRecord record = userMapper.findById(CurrentUser.id());
        return new AuthResponse.UserInfo(record.getUserId(), record.getUsername(), record.getRealName(), record.getRole());
    }

    public void changePassword(ChangePasswordRequest request) {
        if (request == null || request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            throw BusinessException.badRequest("旧密码不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw BusinessException.badRequest("新密码至少需要 6 位");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw BusinessException.badRequest("新密码不能与旧密码相同");
        }
        UserRecord record = userMapper.findById(CurrentUser.id());
        if (record == null) {
            throw BusinessException.unauthorized("未登录");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), record.getPasswordHash())) {
            throw BusinessException.badRequest("旧密码不正确");
        }
        userMapper.updatePasswordHashById(record.getUserId(), passwordEncoder.encode(request.getNewPassword()));
    }
}
