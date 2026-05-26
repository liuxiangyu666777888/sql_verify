from flask import request
from flask_restful import Resource
import hashlib, os
from ..config import *
from .. import models
from ..permissions import auth_role
from . import model_to_dict


class Login(Resource):
    def post(self):
        data = request.get_json()
        user_id = data.get('id')
        password = data.get('password')
        role_str = data.get('role')

        if not user_id or not password or not role_str:
            return {"message": "缺少用户名，密码或身份"}, HTTP_BAD_REQUEST

        role_map = {"student": 0, "teacher": 1, "admin": 2, "assistant": 3}
        if role_str not in role_map:
            return {"message": "非法身份信息"}, HTTP_BAD_REQUEST
        role = role_map[role_str]

        try:
            user_id = int(user_id)
        except ValueError:
            return {"message": "用户ID须为整数"}, HTTP_BAD_REQUEST

        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "用户名或密码无效"}, HTTP_UNAUTHORIZED

        hashed = hashlib.sha256(password.encode('utf8')).hexdigest()
        if user.password != hashed:
            return {"message": "用户名或密码无效"}, HTTP_UNAUTHORIZED

        if user.role != role:
            return {"message": "用户身份不匹配，请重新选择！"}, HTTP_UNAUTHORIZED

        session_token = hashlib.sha1(os.urandom(24)).hexdigest()
        user.session = session_token
        db.session.commit()
        return model_to_dict(user), HTTP_OK

    def delete(self):
        session = request.headers.get('session')
        user = models.User.query.filter_by(session=session).first()
        if user:
            user.session = None
            db.session.commit()
            return {"message": '成功退出登录'}, HTTP_OK
        else:
            return {"message": '身份信息无效！请重新登录。'}, HTTP_UNAUTHORIZED

    def get(self):
        session = request.headers.get('session')
        user = models.User.query.filter_by(session=session).first()
        if user:
            return {"id": user.id, "name": user.username, "role": user.role}, HTTP_OK
        else:
            return {"message": '身份信息无效！请重新登录。'}, HTTP_UNAUTHORIZED


class Register(Resource):
    def post(self):
        id = int(request.json.get('id')) if request.json.get('id') else None
        username = request.json.get('username', None)
        password = hashlib.sha256(request.json.get('password', '').encode('utf8')).hexdigest()
        role_str = request.json.get('role', 'student')
        ROLE_MAP = {"student": AUTH_STUDENT, "teacher": AUTH_TEACHER, "assistant": AUTH_ASSISTANT, "admin": AUTH_ADMIN}
        role = ROLE_MAP.get(role_str)

        if role is None:
            return {"message": "无效的用户身份类型"}, HTTP_BAD_REQUEST
        if models.User.query.filter_by(id=id).first():
            return {"message": "用户编号已被占用，请更换后重试。"}, HTTP_CONFLICT
        if models.User.query.filter_by(username=username).first():
            return {"message": "该用户名已被注册，请选择其他用户名。"}, HTTP_CONFLICT

        new_user = models.User(id=id, username=username, password=password, role=role)
        db.session.add(new_user)
        db.session.commit()

        if role == AUTH_TEACHER:
            msg = "注册成功，欢迎加入 SQL 在线测评平台（教师端）。"
        elif role == AUTH_ASSISTANT:
            msg = "注册成功，欢迎使用 SQL 在线测评平台（助教权限已开启）。"
        else:
            msg = "注册成功，欢迎进入 SQL 在线测评平台，开始你的学习之旅！"
        return {"message": msg}, HTTP_CREATED
