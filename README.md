# SQL Judge Exam

SQL Judge Exam 是一个在线 SQL 判题与考试系统，包含 Spring Boot 后端、Vue 3 前端、MySQL 数据库和 Flyway 数据库迁移脚本。系统支持学生练习与考试作答，教师/助教维护题库、班级、考试和成绩，管理员管理用户与角色。

## 技术栈

- 后端：Java 11、Spring Boot 2.7、Spring Security、MyBatis、Flyway
- 前端：Vue 3、Vite 5、TypeScript、Pinia、Axios
- 数据库：MySQL 8.x
- 判题：MySQL 临时 schema 执行测试用例，只允许查询类 SQL

## 环境要求

- Java 11 或以上
- MySQL 8.x
- Node.js 20 或 22 LTS
- npm
- macOS/Linux shell 环境

macOS 如果安装了 Homebrew `node@20`，建议先执行：

```bash
export PATH="/opt/homebrew/opt/node@20/bin:$PATH"
node -v
```

## 默认账号

| 账号 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| `admin` | `password` | 管理员 | 管理用户、角色和账号状态 |
| `teacher1` | `password` | 教师 | 题库、班级、考试、成绩管理 |
| `student1` | `password` | 学生 | 浏览题目、提交 SQL、参加考试 |

管理员也可以在用户管理页面创建 `ASSISTANT` 助教账号。助教复用教师端工作台和教师权限。

## 一键安装与启动

首次初始化数据库和前端依赖：

```bash
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh --setup
```

启动系统：

```bash
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh
```

启动成功后访问：

- 前端页面：`http://localhost:5173`
- 登录页：`http://localhost:5173/login`
- 后端 API：`http://localhost:8080/api`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

停止服务：

```bash
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh --stop
```

常用环境变量：

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sql_exam
DB_USERNAME=root
DB_PASSWORD=你的MySQL密码
SERVER_PORT=8080
FRONTEND_PORT=5173
JWT_SECRET=local-dev-secret-change-me-local-dev-secret
```

## 手动安装与运行

### 1. 创建数据库

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS sql_exam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. 启动后端

```bash
cd backend
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./mvnw -DskipTests package
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' java -Dfile.encoding=UTF-8 -jar target/exam-0.0.1-SNAPSHOT.jar
```

后端启动时会自动执行 Flyway 迁移：

- `V1__init_schema.sql`：建表
- `V2__seed_data.sql`：初始化账号、班级、示例题
- `V3__fix_seed_passwords.sql`：修复默认账号密码
- `V4__seed_leetcode_questions.sql`：导入 LeetCode SQL 题
- `V5__add_assistant_role.sql`：增加助教角色

### 3. 启动前端

```bash
cd frontend
npm ci --cache /tmp/sqljudge-npm-cache --no-audit --no-fund
VITE_API_BASE_URL=/api npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

## 公网分享

本项目支持通过 ngrok 或 Cloudflare Tunnel 分享本地页面。更推荐 ngrok，因为免费账号可使用固定 dev domain。

ngrok 示例：

```bash
ngrok config add-authtoken '你的ngrok token'
ngrok http http://127.0.0.1:5173
```

如果用公网地址访问，建议把后端 CORS 放行该域名：

```bash
CORS_ALLOWED_ORIGINS='http://localhost:5173,http://127.0.0.1:5173,https://你的公网域名' \
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh
```

前端 Vite 已允许 `.trycloudflare.com` 和 `.ngrok-free.dev` 域名访问。

## 使用说明

### 管理员

1. 访问 `/login`，使用 `admin / password` 登录。
2. 进入 `User Admin`。
3. 创建学生、教师、助教或管理员账号。
4. 编辑用户角色和账号状态。

### 教师/助教

1. 使用教师或助教账号登录。
2. 进入教师工作台。
3. 在 `Problem Bank` 创建题目和测试用例。
4. 在 `Classroom` 创建班级并获取邀请码。
5. 在 `Configure Exam` 创建考试、选题、设置分值、分配学生并发布。
6. 在 `Gradebook` 查看考试成绩。

### 学生

1. 使用 `student1 / password` 登录。
2. 在题库中查看题目并提交 SQL。
3. 在班级页面通过邀请码加入班级。
4. 在考试页面进入已分配考试并作答。
5. 在提交记录页面查看历史提交。

## 功能验证

### 1. 登录验证

```bash
curl -sS -H 'Content-Type: application/json' \
  -d '{"username":"student1","password":"password"}' \
  http://127.0.0.1:8080/api/auth/login
```

返回 `code: 0` 且包含 `token` 表示登录成功。

### 2. 前端页面验证

```bash
curl -I http://127.0.0.1:5173/login
```

返回 `200 OK` 表示前端启动成功。

### 3. 后端 API 验证

```bash
curl -I http://127.0.0.1:8080/v3/api-docs
```

返回 `200 OK` 表示后端启动成功。

### 4. 判题流程验证

1. 学生登录。
2. 打开题库中的示例题。
3. 输入查询 SQL，点击“运行自测”。
4. 点击“提交代码”。
5. 在 `My Submissions` 中查看记录。

示例 SQL：

```sql
SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary
FROM Employee e
JOIN Department d ON e.departmentId = d.id
WHERE (e.departmentId, e.salary) IN (
  SELECT departmentId, MAX(salary)
  FROM Employee
  GROUP BY departmentId
);
```

### 5. 考试流程验证

1. 教师或助教创建并发布考试。
2. 学生进入 `Learning Paths`。
3. 在考试时间窗口内进入作答页。
4. 提交后教师/助教在 `Gradebook` 查看成绩。

## 构建与测试

后端编译打包：

```bash
cd backend
./mvnw -DskipTests package
```

前端生产构建：

```bash
cd frontend
npm run build
```

LaTeX 项目文档生成：

```bash
cd 项目文档
latexmk -xelatex -interaction=nonstopmode -halt-on-error '项目文档.tex'
latexmk -c '项目文档.tex'
```

如果需要完整本地验收，建议依次执行：

```bash
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh --setup
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh
```

然后按“功能验证”章节检查登录、判题、考试、成绩和用户管理。

## 常见问题

### MySQL 连接失败

确认 MySQL 已启动，并传入真实密码：

```bash
DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh --setup
```

### 端口被占用

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -nP -iTCP:5173 -sTCP:LISTEN
```

如果是本项目残留服务：

```bash
./start.sh --stop
```

### Flyway 校验失败

不要直接修改已经执行过的 `V1/V2/...` 迁移脚本。需要变更数据库结构时新增更高版本迁移文件，例如 `V5__add_assistant_role.sql`。

本地开发库如果因历史脚本变动导致校验失败，可在确认数据可接受后执行 Flyway repair：

```bash
cd backend
./mvnw \
  -Dflyway.url='jdbc:mysql://localhost:3306/sql_exam?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  -Dflyway.user=root \
  -Dflyway.password='你的MySQL密码' \
  -Dflyway.locations=filesystem:src/main/resources/db/migration \
  org.flywaydb:flyway-maven-plugin:9.22.3:repair
```

### Vite 启动卡住

确认 Node 使用 LTS 版本，并清理代理环境：

```bash
node -v
unset HTTP_PROXY HTTPS_PROXY http_proxy https_proxy
```

必要时重装前端依赖：

```bash
cd frontend
rm -rf node_modules
npm ci --cache /tmp/sqljudge-npm-cache --no-audit --no-fund
```

### Cloudflare quick tunnel 频繁 530

`trycloudflare.com` quick tunnel 无稳定性保证，断开后地址可能失效。建议改用 ngrok 固定 dev domain，或使用 Cloudflare Named Tunnel 绑定自己的域名。

## 目录说明

- `backend/`：Spring Boot 后端、MyBatis Mapper、Flyway 迁移
- `frontend/`：Vue 3 前端工程
- `项目文档/`：LaTeX 项目文档、ER 图和生成后的 PDF
- `SQL题目.md`：原始 SQL 题目材料
- `start.sh`：一键初始化、启动和停止脚本
- `改进.md`：当前实现与后续改进说明
