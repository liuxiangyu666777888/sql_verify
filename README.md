# SQL Judge Exam

本项目是一个在线 SQL 判题与考试系统，分为 `backend/` 与 `frontend/` 两部分。

## 目录

- `backend/`: Spring Boot 2.7 + Java 11 + MyBatis + Flyway
- `frontend/`: Vue 3 + Vite + TypeScript + Tailwind
- `db/`: 额外数据库脚本
- `front/`: 现有原型参考页面

## 默认账号

- `admin / password`
- `teacher1 / password`
- `student1 / password`

## 启动说明

### 后端

1. 配置本地 MySQL，创建空库或允许 Flyway 自动建库。
2. 进入 `backend/`，使用 Maven Wrapper 启动。
3. 默认端口 `8080`。

### 前端

1. 安装 Node.js。
2. 进入 `frontend/`。
3. `npm install`
4. `npm run dev`

### 说明

- 第一版判题仅支持查询类 SQL。
- `front/` 下 HTML 仅作参考，最终页面以 `frontend/` 工程为准。
