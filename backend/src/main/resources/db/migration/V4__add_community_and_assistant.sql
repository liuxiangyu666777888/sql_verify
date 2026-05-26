USE sql_exam;

-- 助教-学生关联表
CREATE TABLE IF NOT EXISTS assistant_students (
    assistant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (assistant_id, student_id),
    KEY idx_assistant_students_assistant (assistant_id),
    KEY idx_assistant_students_student (student_id),
    CONSTRAINT fk_assistant_students_assistant
        FOREIGN KEY (assistant_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_assistant_students_student
        FOREIGN KEY (student_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- 社区文章表
CREATE TABLE IF NOT EXISTS articles (
    article_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NULL,
    is_notice TINYINT(1) NOT NULL DEFAULT 0,
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_articles_user (user_id),
    KEY idx_articles_question (question_id),
    CONSTRAINT fk_articles_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_articles_question
        FOREIGN KEY (question_id) REFERENCES questions(question_id)
        ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB;
