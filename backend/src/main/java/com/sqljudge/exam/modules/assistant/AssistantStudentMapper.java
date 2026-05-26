package com.sqljudge.exam.modules.assistant;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssistantStudentMapper {

    @Select("SELECT a.*, u.username AS student_name FROM assistant_students a JOIN users u ON u.user_id = a.student_id WHERE a.assistant_id = #{assistantId}")
    List<AssistantStudentRecord> listByAssistant(@Param("assistantId") Long assistantId);

    @Select("SELECT a.*, u.username AS assistant_name FROM assistant_students a JOIN users u ON u.user_id = a.assistant_id WHERE a.student_id = #{studentId}")
    List<AssistantStudentRecord> listByStudent(@Param("studentId") Long studentId);

    @Insert("INSERT INTO assistant_students (assistant_id, student_id) VALUES (#{assistantId}, #{studentId})")
    int insert(@Param("assistantId") Long assistantId, @Param("studentId") Long studentId);

    @Delete("DELETE FROM assistant_students WHERE assistant_id = #{assistantId} AND student_id = #{studentId}")
    int delete(@Param("assistantId") Long assistantId, @Param("studentId") Long studentId);

    @Delete("DELETE FROM assistant_students WHERE student_id = #{studentId}")
    int deleteByStudent(@Param("studentId") Long studentId);
}
