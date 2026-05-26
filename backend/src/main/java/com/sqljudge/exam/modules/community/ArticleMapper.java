package com.sqljudge.exam.modules.community;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper {

    @Select("SELECT a.*, u.username FROM articles a JOIN users u ON u.user_id = a.user_id WHERE a.article_id = #{id}")
    ArticleRecord findById(@Param("id") Long id);

    @Select("SELECT a.*, u.username FROM articles a JOIN users u ON u.user_id = a.user_id ORDER BY a.publish_time DESC")
    List<ArticleRecord> listAll();

    @Select("SELECT a.*, u.username FROM articles a JOIN users u ON u.user_id = a.user_id WHERE a.user_id = #{userId} ORDER BY a.publish_time DESC")
    List<ArticleRecord> listByUser(@Param("userId") Long userId);

    @Insert("INSERT INTO articles (title, content, user_id, question_id, is_notice) VALUES (#{title}, #{content}, #{userId}, #{questionId}, #{isNotice})")
    @Options(useGeneratedKeys = true, keyProperty = "articleId")
    int insert(ArticleRecord article);

    @Update("UPDATE articles SET content = #{content}, last_modify_time = NOW() WHERE article_id = #{articleId}")
    int updateContent(@Param("articleId") Long articleId, @Param("content") String content);

    @Delete("DELETE FROM articles WHERE article_id = #{id}")
    int deleteById(@Param("id") Long id);
}
