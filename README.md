# SQL Judge Exam

在线 SQL 判题与考试系统。项目包含 Spring Boot 后端、Vue 3 前端、MySQL 数据库和 Flyway 初始化脚本。

## 技术栈

- 后端：Spring Boot 2.7、Java 11、MyBatis、Flyway
- 前端：Vue 3、Vite 5、TypeScript、Pinia
- 数据库：MySQL 8.x
- 判题：临时 schema 执行测试用例，当前只允许查询类 SQL

## 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `password` | 管理员 |
| `teacher1` | `password` | 教师 |
| `student1` | `password` | 学生 |

## 环境要求

- Java 11+
- MySQL 8.x
- Node.js 20 或 22 LTS
- macOS 推荐用 Homebrew 的 `node@20`

不建议使用 Node 25。本项目前端固定使用 `vite@5.4.21` 和 `@vitejs/plugin-vue@5.2.4`。

## 一键启动

如果你本机安装了 Homebrew `node@20`，先执行：

```bash
export PATH="/opt/homebrew/opt/node@20/bin:$PATH"
node -v
```

首次初始化：

```bash
DB_USERNAME=root DB_PASSWORD='123456' ./start.sh --setup
```

启动系统：

```bash
DB_USERNAME=root DB_PASSWORD='123456' ./start.sh
```

启动成功后访问：

- 前端页面：`http://localhost:5173`
- 登录页：`http://localhost:5173/login`
- 后端 API：`http://localhost:8080/api`
- Swagger：`http://localhost:8080/swagger-ui.html`

停止服务：

```bash
DB_USERNAME=root DB_PASSWORD='123456' ./start.sh --stop
```

也可以在启动脚本所在终端按 `Ctrl+C`。

## 常用环境变量

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sql_exam
DB_USERNAME=root
DB_PASSWORD=123456
SERVER_PORT=8080
FRONTEND_PORT=5173
```

示例：

```bash
DB_USERNAME=root DB_PASSWORD='123456' SERVER_PORT=8081 FRONTEND_PORT=5174 ./start.sh
```

## 题库数据

题目存储在数据库中，不写死在前端。后端启动时 Flyway 会自动执行：

- `V1__init_schema.sql`：建表
- `V2__seed_data.sql`：基础账号、班级、示例题
- `V3__fix_seed_passwords.sql`：修复默认账号密码
- `V4__seed_leetcode_questions.sql`：导入 `SQL题目.md` 前 10 道 LeetCode SQL 题

已导入题号：

```text
175, 176, 177, 178, 180, 181, 182, 183, 184, 185
```

其中 177 原题是函数题，当前平台只允许 `SELECT`，所以导入为固定 `N = 2` 的练习版。

## 手动启动

后端：

```bash
cd backend
DB_USERNAME=root DB_PASSWORD='123456' ./mvnw -DskipTests package
DB_USERNAME=root DB_PASSWORD='123456' java -Dfile.encoding=UTF-8 -jar target/exam-0.0.1-SNAPSHOT.jar
```

前端：

```bash
cd frontend
npm ci --cache /tmp/sqljudge-npm-cache --no-audit --no-fund
npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

## 常见问题

### 端口被占用

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -nP -iTCP:5173 -sTCP:LISTEN
```

如果是本项目残留服务，执行：

```bash
DB_USERNAME=root DB_PASSWORD='123456' ./start.sh --stop
```

### 前端启动超时

先确认 Node 是 LTS：

```bash
node -v
```

然后重装前端依赖：

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install --cache /tmp/sqljudge-npm-cache --no-audit --no-fund
cd ..
```

如果本机配置了不可用代理，先临时清掉：

```bash
unset HTTP_PROXY HTTPS_PROXY http_proxy https_proxy NODE_TLS_REJECT_UNAUTHORIZED
```

### `.pids` 出现 stale handle

删除临时 pid/log 目录后重启：

```bash
rm -rf .pids
DB_USERNAME=root DB_PASSWORD='123456' ./start.sh
```

### Maven 打包异常

清理后重新打包：

```bash
rm -rf backend/target
cd backend
./mvnw -q -DskipTests package
```

### MySQL 连接失败

确认 MySQL 已启动，并传入真实密码：

```bash
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh
```

## 目录说明

- `backend/`：后端服务和 Flyway 迁移
- `frontend/`：Vue 前端工程
- `front/`：早期 HTML 原型
- `SQL题目.md`：原始 SQL 题目文档
- `design.md`：设计说明
- `start.sh`：一键启动脚本
