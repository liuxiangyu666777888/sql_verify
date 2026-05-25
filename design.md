# 在线 SQL 判题考试系统前后端详细设计方案

## 1. 设计依据与目标

本方案参考 `第三组在线SQL判题考试系统数据库设计报告.pdf` 中的系统定位、9 张核心数据表、角色权限矩阵、业务流程和完整性约束，并结合 `front/` 目录下已经完成的 HTML 原型进行前后端落地设计。

现有前端原型对应页面如下：

| 原型文件 | 对应功能 | 后续页面建议 |
| --- | --- | --- |
| `front/student.html` | 学生工作台：学习进度、即将考试、推荐练习 | `/student/dashboard` |
| `front/sql_verifier.html` | SQL 题目详情、编辑器、自测、提交、判题结果 | `/problems/:questionId` 与 `/exams/:examId/questions/:questionId` |
| `front/exam.html` | 教师创建考试：配置考试参数、选题、分值 | `/teacher/exams/new` 与 `/teacher/exams/:examId/edit` |
| `front/teacher.html` | 教师课堂总览：班级、题库统计、实时提交 | `/teacher/dashboard` |

项目目标：

1. 为数据库课程提供在线 SQL 练习、考试、自动判题和成绩统计能力。
2. 使用 MySQL 作为业务数据库和判题数据执行环境。
3. 以数据库设计报告中的 9 张核心表为主干，补充开发所需字段，避免后续实现时功能信息不足。
4. 让后续 agent 可以直接按本文档拆分任务，实现前端、后端、数据库和判题引擎。

## 2. 推荐技术栈

如果没有额外限制，建议采用以下技术栈。

### 2.1 后端

| 层次 | 技术 | 说明 |
| --- | --- | --- |
| Web 框架 | Spring Boot 3.x | 适合课程管理、权限、事务、MySQL 集成 |
| 语言 | Java 17+ | 稳定，生态完整 |
| ORM/SQL | MyBatis 或 MyBatis-Plus | 便于写复杂统计 SQL，也方便保留对数据库的可控性 |
| 鉴权 | JWT + Spring Security | 支持学生、教师、管理员、助教多角色权限 |
| 数据库 | MySQL 8.x | 支持 CHECK、JSON、窗口函数 |
| API 风格 | RESTful JSON | 前后端分离，便于 agent 独立开发 |
| 文档 | OpenAPI/Swagger | 自动生成接口文档 |
| 判题执行 | 后端 JudgeService + 独立 MySQL 低权限账号 | 必须与业务库隔离 |

也可以使用 Node.js/NestJS 实现后端，但后续实现时需要保持本文档定义的数据结构、接口和安全策略不变。

### 2.2 前端

| 层次 | 技术 | 说明 |
| --- | --- | --- |
| 构建工具 | Vite | 开发速度快 |
| 框架 | Vue 3 + TypeScript | 适合后台系统和状态驱动页面 |
| 样式 | Tailwind CSS | 复用现有 HTML 中的设计 token |
| 路由 | Vue Router | 学生端、教师端、考试端多页面 |
| 状态 | Pinia | 管理登录用户、考试状态、题目详情、提交结果 |
| 编辑器 | Monaco Editor | SQL 高亮、行号、格式化、快捷键 |
| HTTP | Axios | 统一拦截 JWT、错误码、加载状态 |
| 图表 | ECharts | 教师统计、学生进度、题库难度分布 |

## 3. 系统角色与权限

角色来自 PDF 报告：学生、教师、管理员、助教。系统采用最小权限原则。

| 角色 | 核心权限 |
| --- | --- |
| 学生 `STUDENT` | 注册登录、查看班级、申请/加入班级、浏览题库、练习题目、参加考试、提交 SQL、查看个人提交和成绩 |
| 教师 `TEACHER` | 创建/管理班级、维护题库和测试用例、创建考试、分配题目和分值、查看学生提交、查看班级统计 |
| 管理员 `ADMIN` | 用户增删改查、角色分配、密码重置、系统配置、数据备份、权限审计 |
| 助教 `ASSISTANT` | 查看授权班级、查看提交记录和成绩统计，默认不允许创建题目和考试 |

后端必须在接口层做角色校验，前端只做显示控制，不能依赖前端隐藏按钮来保证安全。

## 4. 总体架构

推荐采用前后端分离架构。

```text
Browser
  |
  | HTTPS / JSON / JWT
  v
Frontend: Vue 3 + Vite + Tailwind + Monaco
  |
  | REST API
  v
Backend: Spring Boot
  |-- Auth/User Module
  |-- Class Module
  |-- Question/TestCase Module
  |-- Exam Module
  |-- Submission Module
  |-- Judge Module
  |-- Statistics Module
  |
  | JDBC / MyBatis
  v
MySQL
  |-- business database: sql_exam
  |-- judge database/schema pool: sql_judge_xxx
```

数据库分为两个用途：

1. 业务库 `sql_exam`：保存用户、题目、考试、成绩、提交记录。
2. 判题库 `sql_judge` 或临时 schema：执行学生 SQL。不能在业务库中执行学生提交的 SQL。

## 5. 核心业务流程

### 5.1 学生练习题目

1. 学生进入 `/student/dashboard`，系统调用推荐题目和练习统计接口。
2. 学生点击题目，进入 `/problems/:questionId`。
3. 前端加载题目详情、表结构说明、样例输入输出、历史最佳提交。
4. 学生在 Monaco Editor 中编写 SQL。
5. 点击“运行自测”：后端只执行公开测试用例，返回结果预览，不写入正式成绩。
6. 点击“提交代码”：后端创建 `submissions` 记录，调用判题引擎执行全部测试用例。
7. 判题完成后更新提交状态、分数、耗时、错误信息。
8. 前端展示 Accepted/Wrong Answer/Runtime Error 等结果。

### 5.2 学生参加考试

1. 学生进入工作台，查看即将开始或进行中的考试。
2. 点击进入考试，后端检查：
   - 学生属于考试关联班级或考试学生名单。
   - 当前时间在 `start_time` 与 `end_time` 之间。
   - 若设置 `duration_minutes`，还要检查个人开始时间和截止时间。
3. 若首次进入，创建或更新 `exam_students.started_at`。
4. 学生逐题作答，每次提交写入 `submissions`，其中 `exam_id` 不为空。
5. 单题得分取该学生在本考试该题所有提交中的最高分。
6. 考试结束或学生交卷后，系统汇总所有题最高分，更新 `exam_students.final_score`。

### 5.3 教师创建考试

对应 `front/exam.html` 的三步流程：

1. 配置考试参数：标题、开始时间、结束时间、时长、说明、是否严格考试环境。
2. 选择题目：从题库中筛选题目，可按难度、标签、关键字搜索。
3. 设置分值：为每题设置 `score` 和排序，确认发布。

考试状态：

| 状态 | 说明 |
| --- | --- |
| `DRAFT` | 草稿，可编辑 |
| `PUBLISHED` | 已发布，学生可看到 |
| `ONGOING` | 当前时间处于考试窗口 |
| `FINISHED` | 考试结束，只允许查看统计 |
| `ARCHIVED` | 归档 |

### 5.4 教师管理课堂和统计

对应 `front/teacher.html`：

1. 教师进入课堂总览，查看班级数量、题库数量、待处理提交。
2. 查看班级列表：学生数、平均进度、活跃趋势。
3. 查看题库统计：按 Easy/Medium/Hard 分布。
4. 查看实时提交流：学生、题目、提交状态、时间。
5. 点击班级进入班级详情，查看学生列表、考试成绩、提交分布。

## 6. 数据库物理设计

PDF 报告定义了 9 张核心表：`User`、`Class`、`StudentClass`、`Question`、`TestCase`、`Exam`、`ExamQuestion`、`ExamStudent`、`Submission`。落地时建议使用 snake_case 命名，并补充必要字段。

### 6.1 表命名映射

| 报告实体 | MySQL 表名 |
| --- | --- |
| User | `users` |
| Class | `classes` |
| StudentClass | `student_class` |
| Question | `questions` |
| TestCase | `test_cases` |
| Exam | `exams` |
| ExamQuestion | `exam_questions` |
| ExamStudent | `exam_students` |
| Submission | `submissions` |

### 6.2 建库建议

```sql
CREATE DATABASE IF NOT EXISTS sql_exam
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE DATABASE IF NOT EXISTS sql_judge
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
```

### 6.3 业务库 DDL

```sql
USE sql_exam;

CREATE TABLE users (
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

CREATE TABLE classes (
  class_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_name VARCHAR(128) NOT NULL,
  teacher_id BIGINT NOT NULL,
  semester VARCHAR(64) NULL,
  invite_code VARCHAR(32) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_classes_invite_code (invite_code),
  KEY idx_classes_teacher (teacher_id),
  CONSTRAINT fk_classes_teacher
    FOREIGN KEY (teacher_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE student_class (
  student_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  status ENUM('ACTIVE', 'PENDING', 'REMOVED') NOT NULL DEFAULT 'ACTIVE',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (student_id, class_id),
  KEY idx_student_class_class (class_id),
  CONSTRAINT fk_student_class_student
    FOREIGN KEY (student_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_student_class_class
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE questions (
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
  CONSTRAINT fk_questions_creator
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE test_cases (
  case_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  input_sql MEDIUMTEXT NOT NULL,
  expected_output JSON NOT NULL,
  case_order INT NOT NULL DEFAULT 1,
  is_hidden TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_test_cases_question (question_id),
  CONSTRAINT fk_test_cases_question
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE exams (
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
  CONSTRAINT fk_exams_creator
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_exams_time CHECK (end_time > start_time),
  CONSTRAINT ck_exams_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0)
) ENGINE=InnoDB;

CREATE TABLE exam_questions (
  exam_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  question_order INT NOT NULL DEFAULT 1,
  PRIMARY KEY (exam_id, question_id),
  KEY idx_exam_questions_question (question_id),
  CONSTRAINT fk_exam_questions_exam
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_exam_questions_question
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_exam_questions_score CHECK (score >= 0 AND score <= 100)
) ENGINE=InnoDB;

CREATE TABLE exam_students (
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  final_score DECIMAL(6,2) NOT NULL DEFAULT 0,
  status ENUM('NOT_STARTED', 'ONGOING', 'SUBMITTED', 'ABSENT') NOT NULL DEFAULT 'NOT_STARTED',
  started_at DATETIME NULL,
  submitted_at DATETIME NULL,
  PRIMARY KEY (exam_id, student_id),
  KEY idx_exam_students_student (student_id),
  CONSTRAINT fk_exam_students_exam
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_exam_students_student
    FOREIGN KEY (student_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_exam_students_score CHECK (final_score >= 0)
) ENGINE=InnoDB;

CREATE TABLE submissions (
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
  CONSTRAINT fk_submissions_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_submissions_question
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_submissions_exam
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT ck_submissions_score CHECK (score >= 0)
) ENGINE=InnoDB;
```

### 6.4 重要设计说明

1. `questions.description` 保存题面正文，可用 Markdown 或 HTML。建议第一版使用 Markdown，前端渲染为安全 HTML。
2. `questions.source_schema_sql` 保存题目表结构展示 SQL，比如 `CREATE TABLE Employee ...`，用于前端题面展示。
3. `test_cases.input_sql` 保存初始化测试数据的 SQL，判题前在隔离 schema 中执行。
4. `test_cases.expected_output` 建议保存规范化 JSON，而不是纯文本表格。例如：

```json
{
  "columns": ["Department", "Employee", "Salary"],
  "rows": [
    ["IT", "Jim", 90000],
    ["Sales", "Henry", 80000],
    ["IT", "Max", 90000]
  ],
  "orderSensitive": false
}
```

5. `submissions.exam_id` 可以为空。为空表示日常练习；不为空表示考试提交。
6. `exam_students.final_score` 由系统按考试题目最高得分汇总，不能由前端直接写入。
7. PDF 报告要求外键采用 `ON DELETE RESTRICT ON UPDATE RESTRICT`，本设计保持一致，避免误删造成历史成绩丢失。

## 7. 判题引擎设计

SQL 判题是本项目风险最高的部分，必须单独设计。

### 7.1 MVP 判题范围

第一版建议只支持查询类题目：

1. 允许：`SELECT`、`WITH`、`UNION`、`JOIN`、`GROUP BY`、窗口函数等查询语句。
2. 禁止：`INSERT`、`UPDATE`、`DELETE`、`DROP`、`ALTER`、`CREATE`、`TRUNCATE`、`GRANT`、`REVOKE`、`CALL`、`LOAD DATA`、多语句执行。
3. 学生 SQL 不允许访问业务库，只能访问判题临时 schema。

后续如要支持 DML 类题目，需要单独扩展执行策略和结果比对方式。

### 7.2 判题执行流程

```text
submit SQL
  -> 创建 submissions(status=PENDING)
  -> 加载 question + test_cases + exam_question score
  -> SQL 安全检查
  -> 对每个 test case:
       1. 创建临时 schema，如 judge_20260525_xxx
       2. 执行 input_sql 初始化表和数据
       3. 使用低权限 MySQL 用户执行学生 SQL
       4. 设置查询超时，如 2-5 秒
       5. 读取结果集，转为规范 JSON
       6. 与 expected_output 比对
       7. 删除临时 schema 或清空临时表
  -> 计算通过用例数和分数
  -> 更新 submissions(status, score, runtime_ms, error_message, result_preview)
  -> 若是考试提交，重新计算该学生该考试 final_score
```

### 7.3 结果比对规则

1. 列名必须匹配。可配置是否区分大小写，默认不区分。
2. 列数量必须一致。
3. 行数量必须一致。
4. 若 `orderSensitive=false`，按整行 JSON 字符串排序后比较。
5. 数字类型需要规范化，比如 `1` 和 `1.0` 可视为相等。
6. `NULL` 必须与 `NULL` 匹配。
7. 字符串默认精确匹配，去除 MySQL 返回值外层格式，不随意 trim 内部空格。

### 7.4 安全策略

必须执行：

1. 判题 MySQL 用户只拥有 `sql_judge` 下 schema 的权限，绝不能拥有 `sql_exam` 权限。
2. 每次执行学生 SQL 使用单独连接，设置 `setQueryTimeout`。
3. 禁止 JDBC `allowMultiQueries=true`。
4. SQL 提交前做语法级检查，至少用关键字黑名单兜底，优先使用 SQL parser。
5. 限制返回行数，例如最大 1000 行，避免超大结果拖垮服务。
6. 限制初始化 SQL 只由教师创建，学生不可编辑。
7. 判题失败也必须清理临时 schema，清理动作放在 `finally`。

## 8. 后端模块设计

### 8.1 模块划分

| 模块 | 职责 | 主要表 |
| --- | --- | --- |
| Auth 模块 | 登录、注册、JWT、密码加密、当前用户信息 | `users` |
| User 模块 | 用户管理、角色分配、状态管理 | `users` |
| Class 模块 | 班级 CRUD、学生加入/移除、邀请码 | `classes`, `student_class` |
| Question 模块 | 题库 CRUD、测试用例 CRUD、题目搜索 | `questions`, `test_cases` |
| Exam 模块 | 考试创建、发布、选题、分值、考生名单、交卷 | `exams`, `exam_questions`, `exam_students` |
| Submission 模块 | 提交记录、提交详情、历史列表 | `submissions` |
| Judge 模块 | 自测、正式提交、判题执行、结果比对 | `questions`, `test_cases`, `submissions` |
| Statistics 模块 | 学生进度、教师看板、班级成绩、题库统计 | 多表 JOIN |

### 8.2 统一响应格式

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

错误示例：

```json
{
  "code": 40301,
  "message": "当前用户无权访问该考试",
  "data": null
}
```

建议错误码：

| 错误码 | 含义 |
| --- | --- |
| `40000` | 参数错误 |
| `40100` | 未登录或 token 失效 |
| `40300` | 权限不足 |
| `40400` | 资源不存在 |
| `40900` | 状态冲突，如考试已发布不能删除题目 |
| `50000` | 服务端错误 |
| `51000` | 判题服务错误 |

## 9. REST API 设计

### 9.1 认证接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | 公开 | 学生注册，教师/管理员账号建议由管理员创建 |
| `POST` | `/api/auth/login` | 公开 | 登录并返回 JWT |
| `GET` | `/api/auth/me` | 登录 | 获取当前用户 |
| `POST` | `/api/auth/logout` | 登录 | 前端清除 token，后端可做 token 黑名单 |

登录响应：

```json
{
  "token": "jwt-token",
  "user": {
    "userId": 1,
    "username": "alice",
    "realName": "Alice Chen",
    "role": "STUDENT"
  }
}
```

### 9.2 学生工作台接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/student/dashboard` | 学生 | 学习进度、正确率、连续练习天数、即将考试、推荐题目 |
| `GET` | `/api/student/submissions` | 学生 | 我的提交历史 |
| `GET` | `/api/student/exams` | 学生 | 我的考试列表 |
| `GET` | `/api/student/scores` | 学生 | 我的考试成绩 |

`/api/student/dashboard` 返回建议：

```json
{
  "solvedCount": 142,
  "accuracyRate": 86.4,
  "streakDays": 12,
  "upcomingExams": [
    {
      "examId": 10,
      "examName": "Midterm: Advanced Joins",
      "startTime": "2026-05-26T14:00:00",
      "endTime": "2026-05-26T16:00:00",
      "status": "PUBLISHED"
    }
  ],
  "recommendedQuestions": [
    {
      "questionId": 105,
      "title": "Department Top Three Salaries",
      "difficulty": "MEDIUM",
      "tags": ["JOIN", "DENSE_RANK"]
    }
  ]
}
```

### 9.3 题库接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/questions` | 登录 | 题目列表，支持 keyword、difficulty、tag |
| `GET` | `/api/questions/{id}` | 登录 | 题目详情，不返回隐藏测试用例 |
| `POST` | `/api/questions` | 教师/管理员 | 创建题目 |
| `PUT` | `/api/questions/{id}` | 创建者/管理员 | 编辑题目 |
| `DELETE` | `/api/questions/{id}` | 创建者/管理员 | 逻辑删除或设置 visible=0 |
| `GET` | `/api/questions/{id}/test-cases` | 教师/管理员 | 查看测试用例 |
| `POST` | `/api/questions/{id}/test-cases` | 教师/管理员 | 新增测试用例 |
| `PUT` | `/api/test-cases/{caseId}` | 教师/管理员 | 编辑测试用例 |
| `DELETE` | `/api/test-cases/{caseId}` | 教师/管理员 | 删除测试用例 |

题目详情返回：

```json
{
  "questionId": 184,
  "title": "部门工资最高的员工",
  "description": "编写一个 SQL 查询，找出每个部门工资最高的员工。",
  "difficulty": "MEDIUM",
  "sourceSchemaSql": "CREATE TABLE Employee (...);",
  "tags": ["JOIN", "GROUP BY"],
  "sampleCases": [
    {
      "inputSql": "INSERT INTO Employee ...",
      "expectedOutput": {
        "columns": ["Department", "Employee", "Salary"],
        "rows": [["IT", "Jim", 90000]]
      }
    }
  ]
}
```

### 9.4 判题与提交接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `POST` | `/api/judge/run` | 登录 | 运行公开测试用例，不计入成绩 |
| `POST` | `/api/submissions` | 登录 | 正式提交，写入提交记录 |
| `GET` | `/api/submissions/{id}` | 本人/教师/管理员/助教 | 查看提交详情 |
| `GET` | `/api/questions/{id}/submissions` | 本人/教师/管理员 | 某题提交历史 |

提交请求：

```json
{
  "questionId": 184,
  "examId": null,
  "sqlCode": "SELECT ..."
}
```

提交响应：

```json
{
  "submissionId": 9001,
  "status": "AC",
  "score": 100,
  "runtimeMs": 485,
  "errorMessage": null,
  "resultPreview": {
    "columns": ["Department", "Employee", "Salary"],
    "rows": [
      ["IT", "Jim", 90000],
      ["Sales", "Henry", 80000]
    ]
  }
}
```

### 9.5 班级接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/classes` | 登录 | 学生看自己班级，教师看自己创建班级 |
| `POST` | `/api/classes` | 教师/管理员 | 创建班级 |
| `GET` | `/api/classes/{id}` | 班级成员/教师/管理员/助教 | 班级详情 |
| `PUT` | `/api/classes/{id}` | 教师/管理员 | 修改班级 |
| `POST` | `/api/classes/{id}/students` | 教师/管理员 | 添加学生 |
| `DELETE` | `/api/classes/{id}/students/{studentId}` | 教师/管理员 | 移除学生 |
| `POST` | `/api/classes/join` | 学生 | 使用邀请码加入班级 |

### 9.6 考试接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/exams` | 登录 | 学生看可参加考试，教师看创建的考试 |
| `POST` | `/api/exams` | 教师/管理员 | 创建考试草稿 |
| `GET` | `/api/exams/{id}` | 有权限用户 | 考试详情 |
| `PUT` | `/api/exams/{id}` | 创建者/管理员 | 编辑考试基本信息 |
| `POST` | `/api/exams/{id}/questions` | 创建者/管理员 | 批量设置考试题目和分值 |
| `POST` | `/api/exams/{id}/students` | 创建者/管理员 | 批量设置考生 |
| `POST` | `/api/exams/{id}/publish` | 创建者/管理员 | 发布考试 |
| `POST` | `/api/exams/{id}/start` | 学生 | 学生开始考试 |
| `POST` | `/api/exams/{id}/submit` | 学生 | 学生交卷 |
| `GET` | `/api/exams/{id}/scores` | 教师/管理员/助教 | 成绩列表 |

创建考试请求：

```json
{
  "examName": "CS304 Database Systems - Midterm",
  "startTime": "2026-05-26T09:00:00",
  "endTime": "2026-05-26T11:00:00",
  "durationMinutes": 120,
  "instructions": "请认真阅读题目，提交标准 MySQL 查询。",
  "lockdownEnabled": false
}
```

设置题目请求：

```json
{
  "questions": [
    { "questionId": 104, "score": 20, "questionOrder": 1 },
    { "questionId": 211, "score": 30, "questionOrder": 2 }
  ]
}
```

### 9.7 教师统计接口

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| `GET` | `/api/teacher/dashboard` | 教师/管理员/助教 | 教师总览数据 |
| `GET` | `/api/teacher/classes/{id}/stats` | 教师/管理员/助教 | 班级进度、平均分、活跃度 |
| `GET` | `/api/teacher/submissions/live` | 教师/管理员/助教 | 实时提交流 |
| `GET` | `/api/teacher/questions/stats` | 教师/管理员 | 题库难度统计 |

## 10. 前端页面设计

### 10.1 路由设计

```text
/login
/register

/student/dashboard
/student/submissions
/student/scores
/student/classes

/problems
/problems/:questionId

/exams/:examId/take
/exams/:examId/questions/:questionId

/teacher/dashboard
/teacher/classes
/teacher/classes/:classId
/teacher/questions
/teacher/questions/new
/teacher/questions/:questionId/edit
/teacher/exams
/teacher/exams/new
/teacher/exams/:examId/edit
/teacher/exams/:examId/scores

/admin/users
```

### 10.2 组件拆分

建议将现有 HTML 原型拆分为可复用组件。

| 组件 | 功能 |
| --- | --- |
| `AppLayout` | 页面整体布局，处理登录状态和角色跳转 |
| `SideNav` | 左侧导航，学生/教师按角色显示不同菜单 |
| `TopBar` | 搜索、通知、设置、头像 |
| `StatCard` | 仪表盘统计卡片 |
| `QuestionCard` | 推荐题/题库列表卡片 |
| `QuestionStatement` | 题面、表结构、样例输出 |
| `SqlEditor` | Monaco SQL 编辑器 |
| `JudgeResultPanel` | 测试用例、执行结果、错误信息 |
| `ExamWizard` | 创建考试三步流程容器 |
| `ExamBasicForm` | 考试参数表单 |
| `QuestionPicker` | 选题和筛选 |
| `ScoreAssigner` | 分值设置 |
| `ClassRosterTable` | 班级列表/学生列表 |
| `LiveSubmissionFeed` | 实时提交列表 |
| `DifficultyChart` | 题库难度统计图 |

### 10.3 学生工作台页面

对应 `front/student.html`。

数据来源：

1. `GET /api/student/dashboard`
2. `GET /api/student/exams`
3. `GET /api/questions?recommend=true`

页面状态：

1. 加载中：展示 skeleton。
2. 无考试：Upcoming Exams 区域展示空状态。
3. 有进行中考试：按钮显示“进入考试”。
4. 未到开始时间：按钮 disabled，显示开始时间。
5. 已结束考试：按钮显示“查看成绩”。

交互：

1. 搜索框跳转题库列表并带 keyword。
2. 推荐题点击 `Solve` 进入 `/problems/:questionId`。
3. 即将考试点击进入等待页或考试页。

### 10.4 SQL 判题页面

对应 `front/sql_verifier.html`。

页面布局：

1. 左侧：题目标题、难度、表结构、题目描述、样例。
2. 中间可拖拽分隔条：后续可实现宽度调整。
3. 右侧上方：SQL 编辑器。
4. 右侧下方：测试用例/执行结果面板。

关键功能：

1. 编辑器默认语言显示 MySQL。
2. 自动保存草稿到 localStorage，key 建议为 `draft:question:{questionId}:exam:{examId}`。
3. “运行自测”调用 `/api/judge/run`。
4. “提交代码”调用 `/api/submissions`。
5. 对考试提交，必须带 `examId`。
6. 提交后展示 status、runtime、score、resultPreview、errorMessage。

### 10.5 教师考试配置页面

对应 `front/exam.html`。

三步流程：

1. `Configuration`：考试标题、开始时间、结束时间、时长、严格模式、说明。
2. `Select Problems`：题库搜索、难度筛选、勾选题目。
3. `Assign Points`：设置每题分值、排序、总分预览。

数据保存策略：

1. 第一步点击下一步时创建或更新考试草稿。
2. 第二步只维护前端临时选题列表，点击下一步时调用批量题目接口。
3. 第三步点击发布前校验：
   - 考试名称非空。
   - `end_time > start_time`。
   - 至少选择一道题。
   - 每题分值 >= 0。
   - 总分建议为 100，但不强制，除非课程要求。

### 10.6 教师工作台页面

对应 `front/teacher.html`。

数据来源：

1. `GET /api/teacher/dashboard`
2. `GET /api/teacher/questions/stats`
3. `GET /api/teacher/submissions/live`

展示内容：

1. Active Classes：当前教师班级数量。
2. Total Problems：题库数量。
3. Pending Reviews：异常/待处理提交数。
4. Class Roster Overview：班级列表、学生数、平均进度。
5. Problem Bank Stats：Easy/Medium/Hard 分布。
6. Live Submissions：最近提交。

## 11. 前端状态管理

### 11.1 Auth Store

字段：

```ts
interface AuthState {
  token: string | null
  user: {
    userId: number
    username: string
    realName?: string
    role: 'STUDENT' | 'TEACHER' | 'ADMIN' | 'ASSISTANT'
  } | null
}
```

动作：

1. `login(username, password)`
2. `logout()`
3. `fetchMe()`
4. `hasRole(...roles)`

### 11.2 Exam Store

管理考试草稿、选题、分值和考试作答状态。

```ts
interface ExamDraftState {
  examId?: number
  basic: ExamBasicForm
  selectedQuestions: SelectedQuestion[]
  currentStep: 1 | 2 | 3
}
```

### 11.3 Judge Store

管理当前题目的 SQL 草稿、运行结果、提交中状态。

```ts
interface JudgeState {
  questionId: number
  examId?: number
  sqlCode: string
  running: boolean
  submitting: boolean
  lastResult?: JudgeResult
}
```

## 12. 后端权限校验规则

### 12.1 题目权限

1. 学生只能查看 `visible=1` 的题目。
2. 教师可以查看自己创建的所有题目。
3. 管理员可以查看全部题目。
4. 测试用例明文只允许教师、管理员查看。

### 12.2 考试权限

1. 学生只能访问 `exam_students` 中包含自己的考试。
2. 教师只能管理自己创建的考试。
3. 助教只能查看被授权班级的考试和成绩。第一版如果没有助教授权表，可先让助教只读全部班级数据，后续再细化。
4. 已发布考试不建议允许修改题目和分值。如果必须修改，需要记录审计日志。

### 12.3 提交权限

1. 学生只能查看自己的提交。
2. 教师可以查看自己班级或自己考试下学生提交。
3. 管理员可以查看全部提交。
4. 助教只读提交，不允许改分。

## 13. 统计查询设计

### 13.1 学生 solvedCount

定义：日常练习中至少有一次 `AC` 的不同题目数量。

```sql
SELECT COUNT(DISTINCT question_id) AS solved_count
FROM submissions
WHERE user_id = ?
  AND exam_id IS NULL
  AND status = 'AC';
```

### 13.2 学生正确率

定义：AC 提交数 / 总提交数。

```sql
SELECT
  SUM(status = 'AC') / COUNT(*) * 100 AS accuracy_rate
FROM submissions
WHERE user_id = ?;
```

### 13.3 考试最终成绩

定义：每题取最高提交分，按 `exam_questions.score` 上限汇总。

```sql
SELECT
  eq.exam_id,
  s.user_id AS student_id,
  SUM(best.best_score) AS final_score
FROM exam_questions eq
JOIN (
  SELECT exam_id, question_id, user_id, MAX(score) AS best_score
  FROM submissions
  WHERE exam_id = ?
  GROUP BY exam_id, question_id, user_id
) best
  ON best.exam_id = eq.exam_id
 AND best.question_id = eq.question_id
JOIN submissions s
  ON s.exam_id = best.exam_id
 AND s.question_id = best.question_id
 AND s.user_id = best.user_id
WHERE eq.exam_id = ?
GROUP BY eq.exam_id, s.user_id;
```

实现时可以在应用层更清晰地完成汇总，避免 SQL 复杂度过高。

### 13.4 教师题库难度统计

```sql
SELECT difficulty, COUNT(*) AS count
FROM questions
WHERE visible = 1
GROUP BY difficulty;
```

## 14. 开发任务拆分建议

后续给 agent 开发时，建议按以下顺序拆分。

### 14.1 第一阶段：基础可运行

1. 初始化后端项目：Spring Boot、MySQL 连接、统一响应、异常处理。
2. 执行数据库 DDL，创建 9 张核心表。
3. 实现用户注册、登录、JWT 鉴权。
4. 实现题库列表、题目详情接口。
5. 前端搭建 Vue/Vite/Tailwind，迁移现有 HTML 的视觉样式。
6. 实现登录页、学生工作台、题目详情页静态数据联调。

### 14.2 第二阶段：判题闭环

1. 实现测试用例管理。
2. 实现 JudgeService：初始化 schema、执行 SQL、比对 expected_output。
3. 实现 `/api/judge/run` 和 `/api/submissions`。
4. SQL 判题页面接入 Monaco Editor 和结果面板。
5. 完成学生练习闭环：浏览题目 -> 编写 SQL -> 自测 -> 提交 -> 查看结果。

### 14.3 第三阶段：考试闭环

1. 实现考试 CRUD、选题、分值、发布。
2. 实现考试学生名单。
3. 实现学生进入考试、提交考试题目、交卷。
4. 实现成绩汇总。
5. 前端完成教师考试配置页面和学生考试页面。

### 14.4 第四阶段：课堂与统计

1. 实现班级 CRUD、邀请码加入、学生管理。
2. 实现教师工作台统计接口。
3. 实现班级详情、考试成绩列表、提交流。
4. 补充管理员用户管理。

## 15. 测试方案

### 15.1 后端单元测试

重点测试：

1. Auth：密码加密、登录失败、JWT 解析。
2. 权限：学生不能访问他人考试，教师不能改别人题目。
3. Exam：时间校验、分值校验、发布状态校验。
4. Judge：AC、WA、ERROR、TLE、FORBIDDEN。
5. Score：多次提交取最高分。

### 15.2 判题测试用例

至少覆盖：

1. 正确 SQL，返回 AC。
2. 列名错误，返回 WA。
3. 行数错误，返回 WA。
4. 顺序不同但 `orderSensitive=false`，返回 AC。
5. 语法错误，返回 ERROR。
6. 使用 `DROP TABLE`，返回 FORBIDDEN。
7. 无限或超慢查询，返回 TLE。

### 15.3 前端测试

重点检查：

1. 登录后按角色跳转。
2. token 失效时自动回登录页。
3. 考试未开始/进行中/已结束状态显示正确。
4. SQL 编辑器内容不会因为结果刷新丢失。
5. 移动端基本可用，表格可横向滚动。

## 16. UI 和交互规范

保持现有 HTML 原型的风格：

1. 主色：`#005bc0`，强调色使用 `#1a73e8`。
2. 背景：浅色工作台风格，避免大面积深色。
3. 字体：正文使用 Inter，代码使用 JetBrains Mono。
4. 题面与编辑器采用左右分栏，结果面板在右下方。
5. 教师端以表格、统计卡片、图表为主，保持密集但清晰。
6. 前端所有按钮要有 loading 和 disabled 状态。
7. 错误信息要直接显示用户可理解的原因，比如“SQL 语法错误”“考试已结束，不能提交”。

## 17. 环境变量建议

后端：

```env
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sql_exam
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=replace-with-long-random-secret
JUDGE_DB_HOST=localhost
JUDGE_DB_PORT=3306
JUDGE_DB_USERNAME=judge_user
JUDGE_DB_PASSWORD=judge_password
JUDGE_QUERY_TIMEOUT_SECONDS=5
```

前端：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 18. 第一版验收标准

第一版完成后至少应满足：

1. 学生可以注册/登录。
2. 教师可以登录并创建题目、测试用例。
3. 学生可以打开题目详情，在编辑器中提交 SQL。
4. 系统可以用 MySQL 自动判题并返回 AC/WA/ERROR。
5. 教师可以创建考试、选择题目、设置分值并发布。
6. 学生可以参加考试并提交答案。
7. 系统可以汇总考试最终成绩。
8. 教师可以查看班级/考试提交和成绩统计。

## 19. 后续扩展

1. 增加题目标签表，替代 `questions.tags JSON`。
2. 增加助教授权表，实现助教只查看指定班级。
3. 增加通知表，用于考试发布、成绩发布、系统通知。
4. 增加审计日志，记录管理员改角色、教师修改考试等高风险操作。
5. 增加导入导出：Excel 导入学生名单、导出成绩。
6. 增加更强的判题沙箱：容器隔离、资源限制、异步队列。
7. 增加 AI 辅助反馈，但考试场景必须可关闭。
