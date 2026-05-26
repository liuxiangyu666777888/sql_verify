from flask import request, jsonify
from flask_restful import Resource
import hashlib
from ..config import *
from .. import models
from ..permissions import auth_role
from . import model_to_dict


class ManageUsers(Resource):
    def get(self):
        users = models.User.query.all()
        data = [model_to_dict(user) for user in users]
        return jsonify(data)

    def post(self):
        user_id = int(request.json.get('id'))
        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "未找到用户。"}, HTTP_NOT_FOUND
        db.session.delete(user)
        db.session.commit()
        return {}, HTTP_OK

    def put(self):
        try:
            user_id = int(request.json.get('id'))
            new_role = int(request.json.get('role'))
        except (TypeError, ValueError):
            return {"message": "参数错误：id 和 role 必须是整数。"}, HTTP_BAD_REQUEST
        if new_role not in (0, 1, 2, 3):
            return {"message": "无效的角色值。"}, HTTP_BAD_REQUEST

        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "未找到用户。"}, HTTP_NOT_FOUND
        user.role = new_role
        db.session.commit()
        return {"message": "切换成功。"}, HTTP_OK

    def delete(self):
        user_id = request.args.get('user_id')
        try:
            uid = int(user_id)
        except (TypeError, ValueError):
            return {"message": "user_id 必须是整数。"}, HTTP_BAD_REQUEST

        user = models.User.query.filter_by(id=uid).first()
        if not user:
            return {"message": "未找到用户。"}, HTTP_NOT_FOUND

        for es in models.ExamStudent.query.filter_by(student_id=uid).all():
            db.session.delete(es)
        for eas in models.AssistantStudent.query.filter_by(assistant_id=uid).all():
            db.session.delete(eas)
        for eas in models.AssistantStudent.query.filter_by(student_id=uid).all():
            db.session.delete(eas)
        for sub in models.Submission.query.filter_by(student_id=uid).all():
            db.session.delete(sub)
        for art in models.Article.query.filter_by(user_id=uid).all():
            db.session.delete(art)
        for ex in models.Exam.query.filter_by(teacher_id=uid).all():
            db.session.delete(ex)
        db.session.delete(user)
        db.session.commit()
        return {"message": "删除成功。"}, HTTP_OK


class Student(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        student_id = int(request.args.get('student_id'))
        ret = models.User.query.filter_by(id=student_id).first()
        if ret:
            return model_to_dict(ret), HTTP_OK
        else:
            return {"message": "该学生不存在"}, HTTP_NOT_FOUND

    @auth_role(AUTH_ADMIN)
    def delete(self):
        student_id = int(request.args.get('student_id'))
        ret = models.User.query.filter_by(id=student_id).first()
        if ret:
            db.session.delete(ret)
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "该学生不存在"}, HTTP_NOT_FOUND

    @auth_role(AUTH_ADMIN)
    def post(self):
        student = models.User()
        student.id = int(request.json.get('id'))
        student.password = request.json.get('password')
        student.username = request.json.get('username')
        student.role = AUTH_STUDENT
        if student.id and student.password:
            ret = models.User.query.filter_by(id=student.id).first()
            if ret:
                return {"message": "该学生已存在"}, HTTP_CONFLICT
            db.session.add(student)
            db.session.commit()
            return {}, HTTP_CREATED
        else:
            return {"message": "学生信息不全，补全后提交！"}, HTTP_BAD_REQUEST


class StudentList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        students = models.User.query.filter_by(role=AUTH_STUDENT).all()
        data = []
        for student in students:
            student_dict = model_to_dict(student)
            link = models.AssistantStudent.query.filter_by(student_id=student.id).first()
            if link:
                student_dict['assistant_id'] = link.assistant_id
                assistant = models.User.query.filter_by(id=link.assistant_id).first()
                student_dict['assistant_name'] = assistant.username if assistant else None
            else:
                student_dict['assistant_id'] = None
                student_dict['assistant_name'] = None
            data.append(student_dict)
        return jsonify(data)


class UpdateSettings(Resource):
    def post(self):
        data = request.get_json()
        user_id = data.get('id')
        new_username = data.get('username')
        new_password = data.get('password')

        if not user_id:
            return {"message": "用户ID不能为空"}, 400

        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "用户不存在"}, 404

        if new_username:
            existing_user = models.User.query.filter_by(username=new_username).first()
            if existing_user:
                return {"message": "该用户名已被注册，请选择其他用户名"}, 409
            user.username = new_username

        if new_password:
            hashed_password = hashlib.sha256(new_password.encode('utf8')).hexdigest()
            user.password = hashed_password

        db.session.commit()
        return {"message": "设置已成功更新", "username": user.username, "password": new_password}, 200
