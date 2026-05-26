package com.sqljudge.exam.modules.question;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestCaseMapper {
    @Select("select * from test_cases where question_id = #{questionId} and is_hidden = 0 order by case_order")
    List<TestCaseRecord> listVisibleByQuestionId(@Param("questionId") Long questionId);
}
