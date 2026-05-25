package com.sqljudge.exam.modules.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedUserInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public SeedUserInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensure("admin", "System Admin", "admin@example.com", "ADMIN");
        ensure("teacher1", "Teacher One", "teacher1@example.com", "TEACHER");
        ensure("student1", "Student One", "student1@example.com", "STUDENT");
    }

    private void ensure(String username, String realName, String email, String role) {
        if (userMapper.findByUsername(username) != null) {
            return;
        }
        UserRecord record = new UserRecord();
        record.setUsername(username);
        record.setPasswordHash(passwordEncoder.encode("password"));
        record.setRealName(realName);
        record.setEmail(email);
        record.setRole(role);
        userMapper.insert(record);
    }
}
