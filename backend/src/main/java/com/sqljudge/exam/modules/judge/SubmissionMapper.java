package com.sqljudge.exam.modules.judge;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubmissionMapper {
    @Insert("insert into submissions(user_id, question_id, exam_id, sql_code, status, score, runtime_ms, error_message, result_preview) "
            + "values(#{userId}, #{questionId}, #{examId}, #{sqlCode}, #{status}, #{score}, #{runtimeMs}, #{errorMessage}, #{resultPreview})")
    @Options(useGeneratedKeys = true, keyProperty = "submissionId")
    void insert(SubmissionRecord record);

    @Select("select s.submission_id as submissionId, s.user_id as userId, s.question_id as questionId, s.exam_id as examId, "
            + "s.sql_code as sqlCode, s.status, s.score, s.runtime_ms as runtimeMs, s.error_message as errorMessage, "
            + "s.result_preview as resultPreview, s.submit_time as submitTime, q.title "
            + "from submissions s join questions q on q.question_id = s.question_id "
            + "where s.user_id = #{userId} order by s.submit_time desc limit 50")
    List<Map<String, Object>> listMine(@Param("userId") Long userId);

    @Select("select count(distinct question_id) from submissions where user_id = #{userId} and status = 'AC'")
    int solvedCount(@Param("userId") Long userId);

    @Select("select count(*) from submissions where user_id = #{userId}")
    int totalCount(@Param("userId") Long userId);

    @Select("select count(*) from submissions where user_id = #{userId} and status = 'AC'")
    int acCount(@Param("userId") Long userId);

    @Select("select distinct date(submit_time) from submissions where user_id = #{userId} and status = 'AC' order by date(submit_time) desc limit 90")
    List<java.time.LocalDate> recentAcDates(@Param("userId") Long userId);

    @Select("select count(*) from submissions s join exams e on e.exam_id = s.exam_id where e.creator_id = #{teacherId} and s.status = 'PENDING'")
    int pendingReviewCount(@Param("teacherId") Long teacherId);
}
