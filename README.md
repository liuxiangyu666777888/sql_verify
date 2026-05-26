# SQL Judge Exam

在线 SQL 判题与考试系统。项目分为后端、前端和数据库三部分，`front/` 目录保留了原型参考页面，`design.md` 保存详细设计方案。

## 项目结构

- `backend/`：Spring Boot 2.7 + Java 11 + MyBatis + Flyway
- `frontend/`：Vue 3 + Vite + TypeScript + Tailwind
- `db/`：额外数据库脚本
- `front/`：原型参考 HTML
- `design.md`：完整设计方案

## 功能范围

- 学生登录、看题、写 SQL、运行自测、正式提交
- 教师创建题目、管理测试用例、创建考试、配置分值
- 班级管理、成绩统计、提交记录查看
- MySQL 判题，第一版仅支持查询类 SQL

## 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `password` | 管理员 |
| `teacher1` | `password` | 教师 |
| `student1` | `password` | 学生 |

## 环境要求

- Java 11
- MySQL 8.x
- Node.js 18+（前端需要）
- Maven Wrapper 已包含在后端工程中

## 启动后端

1. 准备一个可连接的 MySQL 8 实例。
2. 配置后端环境变量：

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=sql_exam
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=change-me-to-a-long-random-secret
```

3. 进入 `backend/` 启动：

```bash
./mvnw spring-boot:run
```

4. 后端默认运行在 `http://localhost:8080`

## 启动前端

1. 进入 `frontend/`
2. 安装依赖：

```bash
npm install
```

3. 启动开发服务：

```bash
npm run dev
```

4. 前端默认运行在 `http://localhost:5173`

## 接口与页面

- 登录页：`/login`
- 学生工作台：`/student/dashboard`
- 题目页：`/problems/:id`
- 教师工作台：`/teacher/dashboard`
- 新建考试：`/teacher/exams/new`

## 数据库说明

后端会通过 Flyway 自动建表并初始化数据：

- `users`
- `classes`
- `student_class`
- `questions`
- `test_cases`
- `exams`
- `exam_questions`
- `exam_students`
- `submissions`

判题时使用独立的临时 schema，不直接在业务库上执行学生 SQL。

## 说明

- `design.md` 是完整实现依据，README 只负责快速上手。
- `front/` 下的 HTML 只是原型参考，最终交互以 `frontend/` 工程为准。
- 第一版判题仅支持查询类 SQL，不支持 DDL/DML。

## 常见问题

### 1. 登录失败

确认使用默认账号和密码 `password`，并检查后端是否已启动。

### 2. 前端请求失败

确认 `VITE_API_BASE_URL` 指向 `http://localhost:8080/api`。

### 3. 数据库连不上

确认 MySQL 已启动，`DB_HOST`、`DB_PORT`、`DB_USERNAME`、`DB_PASSWORD` 配置正确。

### 4. Maven 命令不可用

请在 `backend/` 下使用 `./mvnw`，不要依赖系统全局 `mvn`。

