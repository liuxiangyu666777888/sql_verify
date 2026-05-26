from flask import request, jsonify
from flask_restful import Resource
from ..config import *
from .. import models
from ..permissions import auth_role


class ClassResource(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        class_id = int(request.args.get('class_id'))
        c = models.Class.query.filter_by(id=class_id).first()
        if not c:
            return {"message": "班级不存在"}, HTTP_NOT_FOUND
        return {"id": c.id, "name": c.name, "teacher_id": c.teacher_id, "teacher_name": c.teacher.username}, HTTP_OK

    @auth_role(AUTH_TEACHER)
    def post(self):
        data = request.get_json()
        name = data.get('name')
        teacher_id = int(data.get('teacher_id'))
        if not name or not teacher_id:
            return {"message": "班级名称和教师ID不能为空"}, HTTP_BAD_REQUEST
        c = models.Class(name=name, teacher_id=teacher_id)
        db.session.add(c)
        db.session.commit()
        return {"message": "班级创建成功", "id": c.id}, HTTP_CREATED

    @auth_role(AUTH_TEACHER)
    def put(self):
        data = request.get_json()
        class_id = int(data.get('class_id'))
        c = models.Class.query.filter_by(id=class_id).first()
        if not c:
            return {"message": "班级不存在"}, HTTP_NOT_FOUND
        if data.get('name'):
            c.name = data.get('name')
        db.session.commit()
        return {"message": "班级信息更新成功"}, HTTP_OK

    @auth_role(AUTH_TEACHER)
    def delete(self):
        class_id = int(request.args.get('class_id'))
        c = models.Class.query.filter_by(id=class_id).first()
        if not c:
            return {"message": "班级不存在"}, HTTP_NOT_FOUND
        models.StudentClass.query.filter_by(class_id=class_id).delete()
        db.session.delete(c)
        db.session.commit()
        return {}, HTTP_OK


class ClassList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        teacher_id = request.args.get('teacher_id')
        student_id = request.args.get('student_id')
        if teacher_id:
            classes = models.Class.query.filter_by(teacher_id=int(teacher_id)).all()
        elif student_id:
            sc = models.StudentClass.query.filter_by(student_id=int(student_id)).all()
            class_ids = [s.class_id for s in sc]
            classes = models.Class.query.filter(models.Class.id.in_(class_ids)).all() if class_ids else []
        else:
            classes = models.Class.query.all()
        result = []
        for c in classes:
            result.append({
                "id": c.id, "name": c.name,
                "teacher_id": c.teacher_id, "teacher_name": c.teacher.username,
                "student_count": models.StudentClass.query.filter_by(class_id=c.id).count()
            })
        return jsonify(result)


class ClassStudent(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        class_id = int(request.args.get('class_id'))
        sc_list = models.StudentClass.query.filter_by(class_id=class_id).all()
        student_ids = [s.student_id for s in sc_list]
        if not student_ids:
            return jsonify([])
        students = models.User.query.filter(models.User.id.in_(student_ids)).all()
        return jsonify([{"id": s.id, "username": s.username, "role": s.role} for s in students])

    @auth_role(AUTH_TEACHER)
    def post(self):
        data = request.get_json()
        class_id = int(data.get('class_id'))
        student_ids = data.get('student_ids', [])
        if not class_id or not student_ids:
            return {"message": "班级ID和学生ID列表不能为空"}, HTTP_BAD_REQUEST
        added = 0
        for sid in student_ids:
            exists = models.StudentClass.query.filter_by(student_id=int(sid), class_id=class_id).first()
            if not exists:
                db.session.add(models.StudentClass(student_id=int(sid), class_id=class_id))
                added += 1
        db.session.commit()
        return {"message": f"成功添加 {added} 名学生"}, HTTP_CREATED

    @auth_role(AUTH_TEACHER)
    def delete(self):
        class_id = int(request.args.get('class_id'))
        student_id = int(request.args.get('student_id'))
        sc = models.StudentClass.query.filter_by(student_id=student_id, class_id=class_id).first()
        if not sc:
            return {"message": "该学生不在班级中"}, HTTP_NOT_FOUND
        db.session.delete(sc)
        db.session.commit()
        return {}, HTTP_OK
