package com.sqljudge.exam.modules.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from users where username = #{username}")
    UserRecord findByUsername(@Param("username") String username);

    @Select("select * from users where user_id = #{userId}")
    UserRecord findById(@Param("userId") Long userId);

    @Insert("insert into users(username, password_hash, real_name, email, role, status) values(#{username}, #{passwordHash}, #{realName}, #{email}, #{role}, 'ACTIVE')")
    void insert(UserRecord record);
}
