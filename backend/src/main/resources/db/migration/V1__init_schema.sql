CREATE DATABASE IF NOT EXISTS sql_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE sql_exam;

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  role ENUM('STUDENT', 'TEACHER', 'ADMIN', 'ASSISTANT') NOT NULL,
  status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS classes (
  class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_name VARCHAR(128) NOT NULL,
  teacher_id BIGINT NOT NULL,
  semester VARCHAR(64) NULL,
  invite_code VARCHAR(32) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_classes_invite_code (invite_code),
  KEY idx_classes_teacher (teacher_id),
  CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student_class (
  student_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  status ENUM('ACTIVE', 'PENDING', 'REMOVED') NOT NULL DEFAULT 'ACTIVE',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (student_id, class_id),
  KEY idx_student_class_class (class_id),
  CONSTRAINT fk_student_class_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_student_class_class FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS questions (
  question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  description MEDIUMTEXT NOT NULL,
  difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL DEFAULT 'MEDIUM',
  answer_sql MEDIUMTEXT NOT NULL,
  creator_id BIGINT NOT NULL,
  source_schema_sql MEDIUMTEXT NULL,
  tags JSON NULL,
  visible TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_questions_creator (creator_id),
  KEY idx_questions_difficulty (difficulty),
  CONSTRAINT fk_questions_creator FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS test_cases (
  case_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  input_sql MEDIUMTEXT NOT NULL,
  expected_output JSON NOT NULL,
  case_order INT NOT NULL DEFAULT 1,
  is_hidden TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_test_cases_question (question_id),
  CONSTRAINT fk_test_cases_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exams (
  exam_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_name VARCHAR(200) NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  duration_minutes INT NULL,
  instructions MEDIUMTEXT NULL,
  lockdown_enabled TINYINT(1) NOT NULL DEFAULT 0,
  status ENUM('DRAFT', 'PUBLISHED', 'FINISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  creator_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_exams_creator (creator_id),
  KEY idx_exams_time (start_time, end_time),
  CONSTRAINT fk_exams_creator FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_exams_time CHECK (end_time > start_time),
  CONSTRAINT ck_exams_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exam_questions (
  exam_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  question_order INT NOT NULL DEFAULT 1,
  PRIMARY KEY (exam_id, question_id),
  KEY idx_exam_questions_question (question_id),
  CONSTRAINT fk_exam_questions_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_exam_questions_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_exam_questions_score CHECK (score >= 0 AND score <= 100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exam_students (
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  final_score DECIMAL(6,2) NOT NULL DEFAULT 0,
  status ENUM('NOT_STARTED', 'ONGOING', 'SUBMITTED', 'ABSENT') NOT NULL DEFAULT 'NOT_STARTED',
  started_at DATETIME NULL,
  submitted_at DATETIME NULL,
  PRIMARY KEY (exam_id, student_id),
  KEY idx_exam_students_student (student_id),
  CONSTRAINT fk_exam_students_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_exam_students_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_exam_students_score CHECK (final_score >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS submissions (
  submission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  exam_id BIGINT NULL,
  sql_code MEDIUMTEXT NOT NULL,
  status ENUM('PENDING', 'AC', 'WA', 'ERROR', 'TLE', 'FORBIDDEN') NOT NULL DEFAULT 'PENDING',
  score DECIMAL(6,2) NOT NULL DEFAULT 0,
  runtime_ms INT NULL,
  memory_kb INT NULL,
  error_message TEXT NULL,
  result_preview JSON NULL,
  submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_submissions_user_time (user_id, submit_time),
  KEY idx_submissions_question (question_id),
  KEY idx_submissions_exam_user (exam_id, user_id),
  CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_submissions_question FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_submissions_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_submissions_score CHECK (score >= 0)
) ENGINE=InnoDB;

