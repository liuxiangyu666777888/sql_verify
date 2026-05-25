package com.sqljudge.exam.modules.question;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper {
    @Select("<script>"
            + "select * from questions where visible = 1 "
            + "<if test='keyword != null and keyword != \"\"'>and title like concat('%', #{keyword}, '%')</if> "
            + "order by question_id desc"
            + "</script>")
    List<QuestionRecord> listVisible(@Param("keyword") String keyword);

    @Select("select * from questions where question_id = #{questionId}")
    QuestionRecord findById(@Param("questionId") Long questionId);
}
