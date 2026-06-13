#!/usr/bin/env bash
#
# SQL Judge Exam 一键启动脚本
#
# 用法:
#   ./start.sh          启动后端和前端
#   ./start.sh --setup  创建数据库并安装前端依赖
#   ./start.sh --stop   停止由本脚本启动的服务
#
# 常用环境变量:
#   DB_HOST=localhost DB_PORT=3306 DB_NAME=sql_exam
#   DB_USERNAME=root DB_PASSWORD=your_password
#   SERVER_PORT=8080 FRONTEND_PORT=5173
#

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
PID_DIR="$SCRIPT_DIR/.pids"

export DB_HOST="${DB_HOST:-localhost}"
export DB_PORT="${DB_PORT:-3306}"
export DB_NAME="${DB_NAME:-sql_exam}"
export DB_USERNAME="${DB_USERNAME:-root}"
export DB_PASSWORD="${DB_PASSWORD:-password}"
export SERVER_PORT="${SERVER_PORT:-8080}"
export FRONTEND_PORT="${FRONTEND_PORT:-5173}"
export JWT_SECRET="${JWT_SECRET:-local-dev-secret-change-me-local-dev-secret}"
export VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:${SERVER_PORT}/api}"
export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:${FRONTEND_PORT},http://127.0.0.1:${FRONTEND_PORT}}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BACKEND_PID=""
FRONTEND_PID=""
CLEANED_UP=0

log() { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
err() { echo -e "${RED}[ERROR]${NC} $*" >&2; }
step() { echo -e "\n${BLUE}==>${NC} $*"; }

npm_clean_env() {
  env -u HTTP_PROXY -u HTTPS_PROXY -u http_proxy -u https_proxy -u NODE_TLS_REJECT_UNAUTHORIZED "$@"
}

usage() {
  cat <<'EOF'
SQL Judge Exam 一键启动脚本

用法:
  ./start.sh          启动后端和前端
  ./start.sh --setup  创建数据库并安装前端依赖
  ./start.sh --stop   停止由本脚本启动的服务

常用环境变量:
  DB_HOST=localhost DB_PORT=3306 DB_NAME=sql_exam
  DB_USERNAME=root DB_PASSWORD=your_password
  SERVER_PORT=8080 FRONTEND_PORT=5173
EOF
}

require_cmd() {
  local cmd="$1"
  local hint="$2"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    err "未找到 $cmd。$hint"
    exit 1
  fi
}

is_alive() {
  local pid="${1:-}"
  [ -n "$pid" ] && kill -0 "$pid" >/dev/null 2>&1
}

kill_tree() {
  local pid="${1:-}"
  [ -n "$pid" ] || return 0
  is_alive "$pid" || return 0

  if command -v pgrep >/dev/null 2>&1; then
    local child
    for child in $(pgrep -P "$pid" 2>/dev/null || true); do
      kill_tree "$child"
    done
  fi

  kill "$pid" >/dev/null 2>&1 || true
}

stop_pid_file() {
  local name="$1"
  local file="$2"

  if [ ! -f "$file" ]; then
    return 0
  fi

  local pid
  pid="$(cat "$file" 2>/dev/null || true)"
  rm -f "$file"

  if is_alive "$pid"; then
    log "停止${name}进程 PID $pid"
    kill_tree "$pid"
    sleep 1
    if is_alive "$pid"; then
      warn "${name}进程仍在运行，强制终止 PID $pid"
      kill -9 "$pid" >/dev/null 2>&1 || true
    fi
  else
    warn "${name}PID 已失效，清理记录"
  fi
}

stop_managed_services() {
  stop_pid_file "后端" "$PID_DIR/backend.pid"
  stop_pid_file "前端" "$PID_DIR/frontend.pid"
  rm -f "$PID_DIR/backend.log" "$PID_DIR/frontend.log" "$PID_DIR/mysql-check.log"
  rm -f "$PID_DIR/backend-build.log" "$PID_DIR/frontend.log" "$PID_DIR/vite-check.log" "$PID_DIR/start.out" "$PID_DIR/start-debug.out" "$PID_DIR/start-bg-debug.out"
}

cleanup() {
  if [ "$CLEANED_UP" -eq 1 ]; then
    return 0
  fi
  CLEANED_UP=1

  if [ -f "$PID_DIR/backend.pid" ] || [ -f "$PID_DIR/frontend.pid" ]; then
    echo ""
    log "正在停止服务..."
    stop_managed_services
    log "所有服务已关闭"
  elif [ -d "$PID_DIR" ]; then
    rm -f "$PID_DIR/mysql-check.log"
  fi
}

check_java() {
  require_cmd java "请安装 Java 11+。"
  local version_line major
  version_line="$(java -version 2>&1)"
  version_line="${version_line%%$'\n'*}"
  major="$(printf '%s\n' "$version_line" | awk -F'[\".]' '{print $2}')"

  if [ "${major:-0}" -lt 11 ] 2>/dev/null; then
    err "需要 Java 11+，当前版本: $version_line"
    exit 1
  fi
  log "Java $major OK"
}

check_node() {
  require_cmd node "请安装 Node.js 18+。"
  require_cmd npm "请安装 npm。"
  local major
  major="$(node -v | sed 's/^v//' | cut -d. -f1)"

  if ! [[ "$major" =~ ^[0-9]+$ ]] || (( major < 18 )); then
    err "需要 Node.js 18+，当前版本: $(node -v)"
    exit 1
  fi

  if (( major > 22 )); then
    err "当前 Node.js $(node -v) 不是受支持的 LTS 版本。"
    err "本项目的 Vite 开发服务在 Node 25 下会出现端口已监听但 HTTP 不响应的问题。请切换到 Node 20 或 Node 22 后重试。"
    exit 1
  fi

  log "Node.js $(node -v) OK"
}

mysql_exec() {
  MYSQL_PWD="$DB_PASSWORD" mysql --protocol=TCP -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" "$@"
}

check_mysql_connection() {
  require_cmd mysql "请安装 MySQL 客户端，或确认 mysql 命令在 PATH 中。"
  mkdir -p "$PID_DIR"

  if ! mysql_exec -e "SELECT 1;" >/dev/null 2>"$PID_DIR/mysql-check.log"; then
    err "无法连接 MySQL: ${DB_USERNAME}@${DB_HOST}:${DB_PORT}"
    if [ -s "$PID_DIR/mysql-check.log" ]; then
      tail -n 20 "$PID_DIR/mysql-check.log" >&2
    fi
    cat >&2 <<EOF

请确认 MySQL 已启动，并用正确账号密码重新执行，例如:
  DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh --setup
  DB_USERNAME=root DB_PASSWORD='你的MySQL密码' ./start.sh

当前脚本默认密码是 DB_PASSWORD=password；如果你的本机 MySQL 不是这个密码，需要显式传入。
EOF
    exit 1
  fi

  log "MySQL 连接 OK (${DB_USERNAME}@${DB_HOST}:${DB_PORT})"
}

ensure_database() {
  local db_sql db_ident exists
  db_sql="$(printf '%s' "$DB_NAME" | sed "s/'/''/g")"
  db_ident="$(printf '%s' "$DB_NAME" | sed 's/`/``/g')"
  exists="$(mysql_exec -Nse "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$db_sql';" 2>/dev/null | tr -d '[:space:]')"

  if [ "$exists" = "1" ]; then
    log "数据库 $DB_NAME 已存在"
    return 0
  fi

  log "创建数据库 $DB_NAME"
  if ! mysql_exec -e "CREATE DATABASE IF NOT EXISTS \`$db_ident\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >/dev/null; then
    err "数据库 $DB_NAME 不存在，且当前账号无权创建。请手动创建或更换 DB_USERNAME/DB_PASSWORD。"
    exit 1
  fi
}

check_port_free() {
  local port="$1"
  local name="$2"
  local env_name="$3"
  local pids
  pids="$(lsof -nP -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -n "$pids" ]; then
    err "${name}端口 $port 已被占用，进程 PID: $pids"
    err "请先停止占用进程，或用环境变量改端口，例如: ${env_name}=xxxx ./start.sh"
    exit 1
  fi
}

install_frontend_deps_if_needed() {
  if [ ! -f "$FRONTEND_DIR/package.json" ]; then
    err "未找到 $FRONTEND_DIR/package.json"
    exit 1
  fi

  if [ -d "$FRONTEND_DIR/node_modules" ]; then
    log "前端依赖已存在"
    return 0
  fi

  step "安装前端依赖"
  (
    cd "$FRONTEND_DIR"
    if [ -f package-lock.json ]; then
      npm_clean_env npm ci --cache /tmp/sqljudge-npm-cache --no-audit --no-fund
    else
      npm_clean_env npm install --cache /tmp/sqljudge-npm-cache --no-audit --no-fund
    fi
  )
}

check_frontend_toolchain() {
  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    return 0
  fi

  if [ -d "$FRONTEND_DIR/node_modules/@rolldown" ] || [ -d "$FRONTEND_DIR/node_modules/@oxc-project" ]; then
    err "前端 node_modules 中存在 Vite 8 残留依赖，可能导致 Vite ready 后 HTTP 不响应。"
    cat >&2 <<EOF

请先重装前端依赖:
  cd "$FRONTEND_DIR"
  rm -rf node_modules
  npm ci --cache /tmp/sqljudge-npm-cache --no-audit --no-fund

然后重新执行:
  cd "$SCRIPT_DIR"
  ./start.sh
EOF
    exit 1
  fi

  if [ -f "$FRONTEND_DIR/package-lock.json" ] && grep -q '"vite": "\\^8\\|"vite": "8\\|"@vitejs/plugin-vue": "\\^6\\|"@vitejs/plugin-vue": "6' "$FRONTEND_DIR/package-lock.json"; then
    err "前端 package-lock.json 指向 Vite 8 / Vue 插件 6，这会导致当前环境下 Vite 启动卡住。"
    cat >&2 <<EOF

请恢复稳定依赖:
  cd "$FRONTEND_DIR"
  rm -rf node_modules package-lock.json
  npm install --cache /tmp/sqljudge-npm-cache --no-audit --no-fund

然后重新执行:
  cd "$SCRIPT_DIR"
  ./start.sh
EOF
    exit 1
  fi

  if ! (cd "$FRONTEND_DIR" && npm_clean_env node -e "Promise.all([import('vite'), import('vue')]).then(([, vue]) => { if (!vue.computed) throw new Error('Vue dependency is incomplete: missing computed export'); }).catch((error)=>{ console.error(error.message || error); process.exit(1); })" >/dev/null 2>"$PID_DIR/vite-check.log"); then
    err "前端工具链检查失败。当前 node_modules 可能损坏，或 Vite/Rollup 与当前 Node 版本不兼容。"
    if [ -s "$PID_DIR/vite-check.log" ]; then
      tail -n 20 "$PID_DIR/vite-check.log" >&2
    fi
    cat >&2 <<EOF

建议先重装前端依赖:
  cd "$FRONTEND_DIR"
  rm -rf node_modules
  npm ci --cache /tmp/sqljudge-npm-cache --no-audit --no-fund

然后重新执行:
  cd "$SCRIPT_DIR"
  ./start.sh
EOF
    exit 1
  fi
}

setup_project() {
  step "检查运行环境"
  check_java
  check_node
  check_mysql_connection
  ensure_database

  chmod +x "$BACKEND_DIR/mvnw" 2>/dev/null || true
  install_frontend_deps_if_needed
  check_frontend_toolchain

  log "初始化完成。现在可以运行 ./start.sh 启动服务。"
}

start_backend() {
  step "启动后端 (Spring Boot)"
  chmod +x "$BACKEND_DIR/mvnw" 2>/dev/null || true
  mkdir -p "$PID_DIR"

  log "打包后端（如依赖已缓存，通常很快）..."
  if ! (
    cd "$BACKEND_DIR"
    rm -rf target
    ./mvnw -q -DskipTests package >"$PID_DIR/backend-build.log" 2>&1
  ); then
    err "后端打包失败，最近日志如下:"
    tail -n 120 "$PID_DIR/backend-build.log" >&2 || true
    exit 1
  fi

  cp "$BACKEND_DIR/target/exam-0.0.1-SNAPSHOT.jar" "$PID_DIR/backend-run.jar"

  (
    cd "$SCRIPT_DIR"
    java -Dfile.encoding=UTF-8 -jar "$PID_DIR/backend-run.jar" \
      >"$PID_DIR/backend.log" 2>&1
  ) &
  BACKEND_PID=$!
  echo "$BACKEND_PID" >"$PID_DIR/backend.pid"

  log "等待后端启动，日志: $PID_DIR/backend.log"
  local retries=90
  local i
  for ((i = 1; i <= retries; i++)); do
    if ! is_alive "$BACKEND_PID"; then
      err "后端进程已退出，最近日志如下:"
      tail -n 80 "$PID_DIR/backend.log" >&2 || true
      exit 1
    fi

    if curl -fsS "http://127.0.0.1:${SERVER_PORT}/v3/api-docs" >/dev/null 2>&1; then
      log "后端已启动: http://localhost:${SERVER_PORT}"
      return 0
    fi

    sleep 2
  done

  err "后端启动超时，最近日志如下:"
  tail -n 80 "$PID_DIR/backend.log" >&2 || true
  exit 1
}

start_frontend() {
  step "启动前端 (Vue 3 + Vite)"
  install_frontend_deps_if_needed
  check_frontend_toolchain
  mkdir -p "$PID_DIR"

  (
    cd "$FRONTEND_DIR"
    npm_clean_env npm run dev -- --host 127.0.0.1 --port "$FRONTEND_PORT" --strictPort \
      >"$PID_DIR/frontend.log" 2>&1
  ) &
  FRONTEND_PID=$!
  echo "$FRONTEND_PID" >"$PID_DIR/frontend.pid"

  log "等待前端启动，日志: $PID_DIR/frontend.log"
  local retries=120
  local i
  for ((i = 1; i <= retries; i++)); do
    if ! is_alive "$FRONTEND_PID"; then
      err "前端进程已退出，最近日志如下:"
      tail -n 80 "$PID_DIR/frontend.log" >&2 || true
      exit 1
    fi

    if curl -fsS "http://127.0.0.1:${FRONTEND_PORT}" >/dev/null 2>&1; then
      log "前端已启动: http://localhost:${FRONTEND_PORT}"
      return 0
    fi

    sleep 1
  done

  err "前端启动超时，最近日志如下:"
  tail -n 80 "$PID_DIR/frontend.log" >&2 || true
  exit 1
}

print_success() {
  echo ""
  echo -e "${GREEN}============================================${NC}"
  echo -e "${GREEN}  SQL Judge Exam 已启动${NC}"
  echo -e "${GREEN}============================================${NC}"
  echo ""
  echo -e "  后端 API:   ${BLUE}http://localhost:${SERVER_PORT}/api${NC}"
  echo -e "  Swagger:    ${BLUE}http://localhost:${SERVER_PORT}/swagger-ui.html${NC}"
  echo -e "  前端页面:   ${BLUE}http://localhost:${FRONTEND_PORT}${NC}"
  echo -e "  登录页:     ${BLUE}http://localhost:${FRONTEND_PORT}/login${NC}"
  echo ""
  echo -e "  默认账号:   ${YELLOW}admin / teacher1 / student1${NC}"
  echo -e "  默认密码:   ${YELLOW}password${NC}"
  echo ""
  echo -e "  后端日志:   $PID_DIR/backend.log"
  echo -e "  前端日志:   $PID_DIR/frontend.log"
  echo ""
  echo -e "  按 ${RED}Ctrl+C${NC} 停止所有服务"
  echo -e "  或执行 ${YELLOW}./start.sh --stop${NC} 停止"
  echo -e "${GREEN}============================================${NC}"
}

monitor_services() {
  while true; do
    if ! is_alive "$BACKEND_PID"; then
      err "后端进程已停止，最近日志如下:"
      tail -n 80 "$PID_DIR/backend.log" >&2 || true
      exit 1
    fi

    if ! is_alive "$FRONTEND_PID"; then
      err "前端进程已停止，最近日志如下:"
      tail -n 80 "$PID_DIR/frontend.log" >&2 || true
      exit 1
    fi

    sleep 2
  done
}

case "${1:-}" in
  --help|-h)
    usage
    exit 0
    ;;
  --stop)
    if [ -d "$PID_DIR" ]; then
      stop_managed_services
      log "已停止由 start.sh 管理的服务"
    else
      log "没有发现正在运行的服务"
    fi
    exit 0
    ;;
  --setup)
    setup_project
    exit 0
    ;;
  "")
    ;;
  *)
    err "未知参数: $1"
    usage
    exit 1
    ;;
esac

trap cleanup EXIT
trap 'exit 130' INT TERM

step "检查运行环境"
rm -rf "$PID_DIR" 2>/dev/null || true
mkdir -p "$PID_DIR"
check_java
check_node
check_mysql_connection
ensure_database

step "清理旧服务并检查端口"
stop_managed_services
mkdir -p "$PID_DIR"
check_port_free "$SERVER_PORT" "后端" "SERVER_PORT"
check_port_free "$FRONTEND_PORT" "前端" "FRONTEND_PORT"

start_backend
start_frontend
print_success
monitor_services
