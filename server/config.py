from flask_sqlalchemy import SQLAlchemy
db = SQLAlchemy()

HTTP_OK = 200  # 请求成功,并且处理无错误
HTTP_CREATED = 201  # 已创建。成功请求并创建了新的资源
HTTP_ACCEPTED = 202  # 已接受。已经接受请求，但未处理完成
HTTP_PARTIAL_CONTENT = 206  # 部分内容。服务器成功处理了部分GET请求
HTTP_NO_CONTENT = 204

# 400
HTTP_BAD_REQUEST = 400  # 客户端请求的语法错误，服务器无法理解
HTTP_UNAUTHORIZED = 401 # 请求要求用户的身份认证
HTTP_FORBIDDEN = 403    # 服务器理解请求客户端的请求，但是拒绝执行此请求
HTTP_NOT_FOUND = 404     # 没找到
HTTP_CONFLICT = 409     # 冲突
# 500
HTTP_SERVER_ERROR = 500  # 服务器内部错误，无法完成请求

SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://root:7511881@localhost/oj'
DEBUG = True

AUTH_STUDENT = 0
AUTH_TEACHER = 1
AUTH_ADMIN = 2
AUTH_ASSISTANT = 3
AUTH_ALL = 4


JUDGE_PENDING = -1
JUDGE_ACCEPTED = 0
JUDGE_RUNERROR = 1
JUDGE_WRONGANSWER = 2
JUDGE_TIMELIMIT_EXCEED = 3
JUDGE_MEMLIMIT_EXCEED = 4