package com.sqljudge.exam.security;

import com.sqljudge.exam.modules.user.UserMapper;
import com.sqljudge.exam.modules.user.UserRecord;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SqlUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    public SqlUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRecord record = userMapper.findByUsername(username);
        if (record == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new UserPrincipal(record.getUserId(), record.getUsername(), record.getPasswordHash(), record.getRole(), "ACTIVE".equals(record.getStatus()));
    }
}
