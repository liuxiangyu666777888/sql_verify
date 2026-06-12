package com.sqljudge.exam.modules.classes;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClassMapper {
    @Select("select * from classes where teacher_id = #{teacherId} order by class_id desc")
    List<ClassRecord> listByTeacher(@Param("teacherId") Long teacherId);

    @Select("select c.* from classes c join student_class sc on sc.class_id = c.class_id where sc.student_id = #{studentId} order by c.class_id desc")
    List<ClassRecord> listByStudent(@Param("studentId") Long studentId);

    @Select("select * from classes where invite_code = #{inviteCode}")
    ClassRecord findByInviteCode(@Param("inviteCode") String inviteCode);

    @Insert("insert into student_class(student_id, class_id, status) values(#{studentId}, #{classId}, 'ACTIVE') "
            + "on duplicate key update status = 'ACTIVE'")
    void joinClass(@Param("studentId") Long studentId, @Param("classId") Long classId);
}
