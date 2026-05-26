package com.sqljudge.exam.modules.community;

import com.sqljudge.exam.common.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;

    public ArticleService(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public ArticleRecord findById(Long id) {
        return articleMapper.findById(id);
    }

    public List<ArticleRecord> list(Long userId) {
        if (userId != null) return articleMapper.listByUser(userId);
        return articleMapper.listAll();
    }

    public ArticleRecord create(String title, String content, Long questionId, Boolean isNotice) {
        ArticleRecord article = new ArticleRecord();
        article.setTitle(title);
        article.setContent(content);
        article.setUserId(CurrentUser.id());
        article.setQuestionId(questionId);
        article.setIsNotice(isNotice != null && isNotice);
        article.setPublishTime(java.time.LocalDateTime.now());
        article.setLastModifyTime(java.time.LocalDateTime.now());
        articleMapper.insert(article);
        return article;
    }

    public void updateContent(Long id, String content) {
        articleMapper.updateContent(id, content);
    }

    public void delete(Long id) {
        articleMapper.deleteById(id);
    }
}
