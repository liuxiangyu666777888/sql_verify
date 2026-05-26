from flask import request, jsonify
from flask_restful import Resource
from ..config import *
from .. import models
from ..permissions import auth_role


class AssistantStudent(Resource):
    @auth_role(AUTH_TEACHER)
    def post(self):
        data = request.get_json()
        if not isinstance(data, list):
            return {"message": "数据格式应为列表"}, HTTP_BAD_REQUEST
        for item in data:
            student_id = item.get('student_id')
            assistant_id = item.get('assistant_id')
            if not student_id or not assistant_id:
                continue
            existing = models.AssistantStudent.query.filter_by(student_id=student_id).first()
            if existing:
                db.session.delete(existing)
            link = models.AssistantStudent(assistant_id=assistant_id, student_id=student_id)
            db.session.add(link)
        db.session.commit()
        return {"message": "分配成功"}, HTTP_CREATED

    @auth_role(AUTH_ASSISTANT)
    def get(self):
        assistant_id = request.args.get("assistant_id")
        if not assistant_id:
            return {"message": "缺少 assistant_id"}, 400
        links = models.AssistantStudent.query.filter_by(assistant_id=assistant_id).all()
        student_ids = [link.student_id for link in links]
        students = models.User.query.filter(models.User.id.in_(student_ids)).all()
        result = [{"student_id": s.id, "username": s.username} for s in students]
        return jsonify(result)

    @auth_role(AUTH_TEACHER)
    def delete(self):
        student_id = request.json.get("student_id")
        if not student_id:
            return {"message": "缺少 student_id"}, 400
        link = models.AssistantStudent.query.filter_by(student_id=student_id).first()
        if not link:
            return {"message": "该学生未绑定助教"}, 404
        db.session.delete(link)
        db.session.commit()
        return {"message": "解除成功"}, 200


class AssistantStudentList(Resource):
    def get(self):
        assistant_id = request.args.get('assistant_id')
        if not assistant_id:
            return {"message": "缺少 assistant_id 参数"}, HTTP_BAD_REQUEST
        try:
            assistant_id = int(assistant_id)
        except ValueError:
            return {"message": "assistant_id 必须是整数"}, HTTP_BAD_REQUEST
        links = models.AssistantStudent.query.filter_by(assistant_id=assistant_id).all()
        student_ids = [link.student_id for link in links]
        students = models.User.query.filter(models.User.id.in_(student_ids)).all()
        result = [{"id": s.id, "username": s.username, "role": s.role} for s in students]
        return jsonify(result), HTTP_OK
