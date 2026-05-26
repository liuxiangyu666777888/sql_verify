import os
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

# ── Database ──
DB_USER = os.getenv('DB_USER', 'root')
DB_PASS = os.getenv('DB_PASS', 'Hz_20050212')
DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_PORT = os.getenv('DB_PORT', '3306')
DB_NAME = os.getenv('DB_NAME', 'oj')

SQLALCHEMY_DATABASE_URI = f'mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}'

# ── Judge sandbox ──
JUDGE_DB_NAME = os.getenv('JUDGE_DB_NAME', 'test')
JUDGE_DB_URI = f'mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{JUDGE_DB_NAME}'
JUDGE_DB_URI_NO_DB = f'mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}'

# ── Server ──
DEBUG = os.getenv('FLASK_DEBUG', 'true').lower() == 'true'
SECRET_KEY = os.getenv('SECRET_KEY', 'dev-secret-key-change-in-production')

# ── CORS ──
CORS_ORIGIN = os.getenv('CORS_ORIGIN', 'http://localhost:8080')

# ── HTTP status codes ──
HTTP_OK = 200
HTTP_CREATED = 201
HTTP_ACCEPTED = 202
HTTP_PARTIAL_CONTENT = 206
HTTP_NO_CONTENT = 204
HTTP_BAD_REQUEST = 400
HTTP_UNAUTHORIZED = 401
HTTP_FORBIDDEN = 403
HTTP_NOT_FOUND = 404
HTTP_CONFLICT = 409
HTTP_SERVER_ERROR = 500

# ── Roles ──
AUTH_STUDENT = 0
AUTH_TEACHER = 1
AUTH_ADMIN = 2
AUTH_ASSISTANT = 3
AUTH_ALL = 4

# ── Judge status ──
JUDGE_PENDING = -1
JUDGE_ACCEPTED = 0
JUDGE_RUNERROR = 1
JUDGE_WRONGANSWER = 2
JUDGE_TIMELIMIT_EXCEED = 3
JUDGE_MEMLIMIT_EXCEED = 4
