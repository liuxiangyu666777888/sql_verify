package com.sqljudge.exam.modules.exam;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamMapper {
    @Select("select * from exams where creator_id = #{creatorId} order by start_time desc")
    List<ExamRecord> listByCreator(@Param("creatorId") Long creatorId);

    @Select("select e.* from exams e join exam_students es on es.exam_id = e.exam_id where es.student_id = #{studentId} order by e.start_time desc")
    List<ExamRecord> listByStudent(@Param("studentId") Long studentId);

    @Select("select * from exams where exam_id = #{examId}")
    ExamRecord findById(@Param("examId") Long examId);

    @Insert("insert into exams(exam_name, start_time, end_time, duration_minutes, instructions, lockdown_enabled, status, creator_id) "
            + "values(#{examName}, #{startTime}, #{endTime}, #{durationMinutes}, #{instructions}, #{lockdownEnabled}, #{status}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "examId")
    void insert(ExamRecord record);

    @Insert("insert into exam_questions(exam_id, question_id, score, question_order) values(#{examId}, #{questionId}, #{score}, #{questionOrder}) "
            + "on duplicate key update score = values(score), question_order = values(question_order)")
    void insertExamQuestion(@Param("examId") Long examId, @Param("questionId") Long questionId, @Param("score") double score, @Param("questionOrder") int questionOrder);

    @Insert("insert into exam_students(exam_id, student_id, final_score, status) values(#{examId}, #{studentId}, 0, 'NOT_STARTED') "
            + "on duplicate key update status = status")
    void insertExamStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);

    @Update("update exam_students set final_score = (select coalesce(sum((best_score / 100) * eq.score), 0) from (select question_id, max(score) best_score from submissions where exam_id = #{examId} and user_id = #{studentId} group by question_id) t join exam_questions eq on eq.exam_id = #{examId} and eq.question_id = t.question_id) where exam_id = #{examId} and student_id = #{studentId}")
    void recalculateFinalScore(@Param("examId") Long examId, @Param("studentId") Long studentId);

    @Update("update exam_students set status = #{status}, submitted_at = now() where exam_id = #{examId} and student_id = #{studentId}")
    void updateStudentStatus(@Param("examId") Long examId, @Param("studentId") Long studentId, @Param("status") String status);

    @Update("update exam_students set started_at = coalesce(started_at, now()), status = #{status} where exam_id = #{examId} and student_id = #{studentId}")
    void touchStudentStatus(@Param("examId") Long examId, @Param("studentId") Long studentId, @Param("status") String status);

    @Select("select eq.question_id as questionId, q.title, eq.score, eq.question_order as questionOrder from exam_questions eq join questions q on q.question_id = eq.question_id where eq.exam_id = #{examId} order by eq.question_order")
    List<Map<String, Object>> listQuestions(@Param("examId") Long examId);

    @Select("select es.student_id as studentId, u.username, u.real_name as realName, es.final_score as finalScore, es.status from exam_students es join users u on u.user_id = es.student_id where es.exam_id = #{examId} order by es.final_score desc, u.username asc")
    List<Map<String, Object>> listScores(@Param("examId") Long examId);

    @Update("update exams set status = #{status} where exam_id = #{examId}")
    void updateStatus(@Param("examId") Long examId, @Param("status") String status);
}
