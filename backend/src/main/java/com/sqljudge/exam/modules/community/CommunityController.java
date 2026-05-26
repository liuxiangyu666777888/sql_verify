package com.sqljudge.exam.modules.community;

import com.sqljudge.exam.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {
    private final ArticleService articleService;

    public CommunityController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<ArticleRecord> get(@RequestParam Long articleId) {
        ArticleRecord article = articleService.findById(articleId);
        if (article == null) return ApiResponse.fail(40400, "文章不存在");
        return ApiResponse.ok(article);
    }

    @GetMapping("/list")
    public ApiResponse<List<ArticleRecord>> list(@RequestParam(required = false) Long userId) {
        return ApiResponse.ok(articleService.list(userId));
    }

    @PostMapping
    public ApiResponse<ArticleRecord> create(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        if (title == null || content == null) return ApiResponse.fail(40000, "文章标题和内容不能为空");
        Object qId = body.get("questionId");
        Long questionId = qId instanceof Number ? ((Number) qId).longValue() : null;
        Boolean isNotice = body.get("isNotice") instanceof Boolean ? (Boolean) body.get("isNotice") : false;
        ArticleRecord article = articleService.create(title, content, questionId, isNotice);
        return ApiResponse.ok(article);
    }

    @PutMapping
    public ApiResponse<String> update(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("articleId");
        if (idObj == null) return ApiResponse.fail(40000, "缺少 articleId");
        Long id = ((Number) idObj).longValue();
        String content = (String) body.get("content");
        if (content == null) return ApiResponse.fail(40000, "缺少内容");
        articleService.updateContent(id, content);
        return ApiResponse.ok("ok");
    }

    @DeleteMapping
    public ApiResponse<String> delete(@RequestParam Long articleId) {
        articleService.delete(articleId);
        return ApiResponse.ok("ok");
    }
}
