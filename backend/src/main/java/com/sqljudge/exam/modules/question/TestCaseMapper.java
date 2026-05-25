package com.sqljudge.exam.modules.question;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestCaseMapper {
    @Select("select * from test_cases where question_id = #{questionId} order by case_order limit 1")
    TestCaseRecord firstVisibleByQuestionId(@Param("questionId") Long questionId);
}
