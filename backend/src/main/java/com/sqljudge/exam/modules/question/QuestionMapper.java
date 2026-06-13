package com.sqljudge.exam.modules.question;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface QuestionMapper {
    @Select("<script>"
            + "select * from questions where visible = 1 "
            + "<if test='keyword != null and keyword != \"\"'>and (title like concat('%', #{keyword}, '%') or description like concat('%', #{keyword}, '%'))</if> "
            + "order by question_id desc"
            + "</script>")
    List<QuestionRecord> listVisible(@Param("keyword") String keyword);

    @Select("<script>"
            + "select * from questions where visible = 1 "
            + "<if test='keyword != null and keyword != \"\"'>and (title like concat('%', #{keyword}, '%') or description like concat('%', #{keyword}, '%'))</if> "
            + "order by question_id desc limit #{size} offset #{offset}"
            + "</script>")
    List<QuestionRecord> listVisiblePage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>"
            + "select count(*) from questions where visible = 1 "
            + "<if test='keyword != null and keyword != \"\"'>and (title like concat('%', #{keyword}, '%') or description like concat('%', #{keyword}, '%'))</if>"
            + "</script>")
    long countVisible(@Param("keyword") String keyword);

    @Select("select * from questions where question_id = #{questionId}")
    QuestionRecord findById(@Param("questionId") Long questionId);

    @Insert("insert into questions(title, description, difficulty, answer_sql, creator_id, source_schema_sql, tags, visible) "
            + "values(#{title}, #{description}, #{difficulty}, #{answerSql}, #{creatorId}, #{sourceSchemaSql}, cast(#{tags} as json), #{visible})")
    @Options(useGeneratedKeys = true, keyProperty = "questionId")
    void insert(QuestionRecord record);

    @Update("update questions set title = #{title}, description = #{description}, difficulty = #{difficulty}, answer_sql = #{answerSql}, "
            + "source_schema_sql = #{sourceSchemaSql}, tags = cast(#{tags} as json), visible = #{visible} where question_id = #{questionId}")
    void update(QuestionRecord record);

    @Update("update questions set visible = 0 where question_id = #{questionId}")
    void hide(@Param("questionId") Long questionId);
}
