package com.sqljudge.exam.modules.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from users where username = #{username}")
    UserRecord findByUsername(@Param("username") String username);

    @Select("select * from users where user_id = #{userId}")
    UserRecord findById(@Param("userId") Long userId);

    @Select("select * from users order by user_id asc")
    List<UserRecord> listAll();

    @Insert("insert into users(username, password_hash, real_name, email, role, status) values(#{username}, #{passwordHash}, #{realName}, #{email}, #{role}, 'ACTIVE')")
    void insert(UserRecord record);

    @Update("update users set password_hash = #{passwordHash}, status = 'ACTIVE' where username = #{username}")
    void updatePasswordHash(@Param("username") String username, @Param("passwordHash") String passwordHash);

    @Update("update users set password_hash = #{passwordHash}, updated_at = now() where user_id = #{userId}")
    void updatePasswordHashById(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    @Update("update users set role = #{role}, status = #{status}, updated_at = now() where user_id = #{userId}")
    void updateRoleAndStatus(@Param("userId") Long userId, @Param("role") String role, @Param("status") String status);
}
