# SQL 在线判题考试系统 — 后台

## 项目简介

面向数据库课程教学的在线 SQL 判题系统。支持学生在线练习 SQL、参加限时考试，教师管理题库与考试，助教辅助教学。核心价值在于**自动化判题**与**即时反馈**。

## 角色权限

| 角色 | 编号 | 权限 |
|------|------|------|
| 学生 | 0 | 练习、考试、查看成绩 |
| 教师 | 1 | 管理题目、创建考试、班级管理、查看统计 |
| 管理员 | 2 | 用户管理、系统维护 |
| 助教 | 3 | 查看指定学生提交与成绩 |

## 技术栈

- **框架**: Flask + Flask-RESTful
- **ORM**: SQLAlchemy + Flask-SQLAlchemy
- **数据库**: MySQL 8.0+
- **Python**: 3.9+

## 快速开始

### 1. 创建数据库

在 MySQL 中执行建表脚本：

```bash
mysql -u root -p < create_sql.sql
```

### 2. 安装依赖

```bash
pip install -r requirements.txt
```

### 3. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 修改数据库密码等配置
```

### 4. 启动

```bash
python run.py
```

服务默认运行在 `http://localhost:5000`。

## 项目结构

```
├── app/                        # 应用包
│   ├── __init__.py             # 工厂函数 + 路由注册 + 错误处理
│   ├── config.py               # 配置（环境变量驱动）
│   ├── models.py               # 11 个数据模型
│   ├── permissions.py          # 权限装饰器
│   └── resources/              # API 资源（按模块拆分）
│       ├── auth.py             # 登录/注册
│       ├── users.py            # 用户管理
│       ├── questions.py        # 题库管理
│       ├── exams.py            # 考试管理
│       ├── judge.py            # 判题/提交
│       ├── classes.py          # 班级管理
│       ├── community.py        # 社区文章
│       ├── assistant.py        # 助教分配
│       └── dashboard.py        # 教师仪表盘
├── run.py                      # 启动入口
├── requirements.txt            # Python 依赖
├── .env.example                # 环境变量模板
└── create_sql.sql              # 建表脚本
```

## API 文档

### 认证 /api

所有需要认证的接口需在 Header 中传入 `session`（登录后获取）。

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/register` | 注册 | 公开 |
| POST | `/api/login` | 登录 | 公开 |
| GET | `/api/login` | 获取当前用户信息 | ALL |
| DELETE | `/api/login` | 退出登录 | ALL |

### 用户管理 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/manageusers` | 获取所有用户 | — |
| POST | `/api/manageusers` | 删除用户（旧接口） | — |
| PUT | `/api/manageusers` | 修改用户角色 | — |
| DELETE | `/api/manageusers?user_id=` | 删除用户及关联数据 | — |
| GET | `/api/student?student_id=` | 获取学生信息 | ALL |
| POST | `/api/student` | 添加学生 | ADMIN |
| DELETE | `/api/student?student_id=` | 删除学生 | ADMIN |
| GET | `/api/studentlist` | 学生列表（含助教信息） | ALL |
| POST | `/api/updateSettings` | 修改用户名/密码 | ALL |

### 题库 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/question?question_id=` | 获取题目详情 | ALL |
| POST | `/api/question` | 创建题目 | TEACHER |
| DELETE | `/api/question/<id>` | 删除题目 | TEACHER |
| GET | `/api/questionlist` | 题目列表（支持 `student_id` / `teacher_id`） | ALL |
| GET | `/api/answer?question_id=` | 获取题目答案 | ALL |
| DELETE | `/api/answer?question_id=` | 删除答案 | TEACHER |
| POST | `/api/testcase` | 批量添加测试用例 | TEACHER |
| POST | `/api/check-questions` | 批量校验题目 ID 有效性 | — |

### 考试 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/contest?contest_id=` | 获取考试信息 | ALL |
| POST | `/api/contest` | 创建考试 | TEACHER |
| DELETE | `/api/contest/<id>` | 删除考试 | TEACHER |
| GET | `/api/contestlist` | 考试列表（按角色筛选） | ALL |
| GET | `/api/contest-question?contest_id=` | 获取考试题目 | — |
| POST | `/api/contest-question` | 考试添加题目 | TEACHER |
| POST | `/api/contest-student` | 考试添加学生 | TEACHER |
| GET | `/api/contest-student?userId=` | 获取学生参加的考试 | TEACHER |
| GET | `/api/contestscores` | 考试排名 | ALL |
| GET | `/api/getscore` | 获取单科成绩 | ALL |
| POST | `/api/updatescore` | 更新成绩 | ALL |

### 判题与提交 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/submit` | 提交 SQL | ALL |
| GET | `/api/submit` | 获取提交详情 | ALL |
| DELETE | `/api/submit?submit_id=` | 删除提交记录 | ADMIN |
| GET | `/api/submitlist` | 提交列表（支持 `user_id` / `exam_id` / `class_id`） | ALL |
| POST | `/api/judge` | 执行判题 | — |
| GET | `/api/statuscount?student_id=` | 学生提交统计 | ALL |
| GET | `/api/answeredquestions?student_id=` | 学生做过的题目 | ALL |
| POST | `/api/check-students` | 批量校验学生 ID | — |

### 班级 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/class?class_id=` | 获取班级信息 | ALL |
| POST | `/api/class` | 创建班级 | TEACHER |
| PUT | `/api/class` | 更新班级 | TEACHER |
| DELETE | `/api/class?class_id=` | 删除班级 | TEACHER |
| GET | `/api/classlist` | 班级列表（支持 `teacher_id` / `student_id`） | ALL |
| GET | `/api/class-student?class_id=` | 班级学生列表 | ALL |
| POST | `/api/class-student` | 添加学生到班级 | TEACHER |
| DELETE | `/api/class-student` | 移除班级学生 | TEACHER |

### 助教 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/assistantstudents?assistant_id=` | 查看助教的学生 | ASSISTANT |
| POST | `/api/assistantstudents` | 批量分配学生给助教 | TEACHER |
| DELETE | `/api/assistantstudents` | 解除学生绑定 | TEACHER |
| GET | `/api/assistantstudentlist?assistant_id=` | 助教学生详情列表 | — |

### 社区 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/community?article_id=` | 获取文章 | ALL |
| POST | `/api/community` | 发布文章 | ALL |
| PUT | `/api/community` | 编辑文章 | ALL |
| DELETE | `/api/community?article_id=` | 删除文章 | ADMIN |
| GET | `/api/communitylist` | 文章列表（支持 `user_id`） | ALL |

### 统计 /api

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/teacher-dashboard?teacher_id=` | 教师仪表盘（班级/考试/提交综合统计） | TEACHER |

## 数据库表

| 表名 | 说明 | 行数特征 |
|------|------|---------|
| User | 用户 | 核心表 |
| Question | 题目 | 核心表 |
| TestCase | 测试用例 | 一对多关联 Question |
| Exam | 考试 | 核心表 |
| Exam_Question | 考试-题目关联 | 多对多中间表 |
| Exam_Student | 考试-学生关联 | 多对多中间表 |
| Submission | 提交记录 | 高频写入 |
| Class | 班级 | 一对多关联 User |
| Student_Class | 学生-班级关联 | 多对多中间表 |
| Article | 社区文章 | 内容表 |
| Assistant_Student | 助教-学生关联 | 分配关系表 |

所有外键列均已建立索引。

## 判题流程

```
1. 前端 POST /api/submit → 创建 Submission (status=PENDING)
2. 前端 POST /api/judge  {
     submit_id, submit_sql, question_id, [create_code]
   }
3. 判题引擎：
   a. 创建 sandbox 数据库 test（仅首次）
   b. 对每个测试用例：
      - 清空 test 库所有表
      - 执行 create_code（DDL 建表）
      - 执行 input_sql（插入测试数据）
      - 执行 submit_sql（学生答案）
      - 比对输出与预期
   c. 更新 Submission.pass_rate 和 .status
4. 前端根据结果展示 AC/WA/RE/TLE/MLE
```

## 配置说明

所有配置通过环境变量控制（`.env` 文件或系统变量）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_USER | root | 数据库用户 |
| DB_PASS | Hz_20050212 | 数据库密码 |
| DB_HOST | localhost | 数据库地址 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | oj | 主数据库名 |
| JUDGE_DB_NAME | test | 判题沙箱库名 |
| CORS_ORIGIN | http://localhost:8080 | 前端地址 |
| FLASK_DEBUG | true | 调试模式 |
| SECRET_KEY | (dev key) | Flask 密钥 |
