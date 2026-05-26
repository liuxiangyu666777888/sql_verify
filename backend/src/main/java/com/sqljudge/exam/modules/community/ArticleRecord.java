package com.sqljudge.exam.modules.community;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleRecord {
    private Long articleId;
    private String title;
    private String content;
    private Long userId;
    private Long questionId;
    private Boolean isNotice;
    private LocalDateTime publishTime;
    private LocalDateTime lastModifyTime;
    // joined
    private String username;
}
