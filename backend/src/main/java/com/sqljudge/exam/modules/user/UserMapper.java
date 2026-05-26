package com.sqljudge.exam.modules.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("select * from users where username = #{username}")
    UserRecord findByUsername(@Param("username") String username);

    @Select("select * from users where user_id = #{userId}")
    UserRecord findById(@Param("userId") Long userId);

    @Insert("insert into users(username, password_hash, real_name, email, role, status) values(#{username}, #{passwordHash}, #{realName}, #{email}, #{role}, 'ACTIVE')")
    void insert(UserRecord record);

    @Update("update users set password_hash = #{passwordHash}, status = 'ACTIVE' where username = #{username}")
    void updatePasswordHash(@Param("username") String username, @Param("passwordHash") String passwordHash);

    @Select("select * from users order by user_id")
    java.util.List<UserRecord> listAll();

    @Select("select * from users where role = #{role} order by user_id")
    java.util.List<UserRecord> listByRole(@Param("role") String role);

    @Update("update users set role = #{role} where user_id = #{userId}")
    void updateRole(@Param("userId") Long userId, @Param("role") String role);

    @Update("update users set username = #{username}, password_hash = #{passwordHash} where user_id = #{userId}")
    void updateProfile(@Param("userId") Long userId, @Param("username") String username, @Param("passwordHash") String passwordHash);

    @Delete("delete from users where user_id = #{userId}")
    void deleteById(@Param("userId") Long userId);
}
