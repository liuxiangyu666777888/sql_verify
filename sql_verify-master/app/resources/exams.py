from flask import request, jsonify
from flask_restful import Resource
from sqlalchemy.exc import SQLAlchemyError
from ..config import *
from .. import models
from ..permissions import auth_role
from . import model_to_dict


class Contest(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        contest_id = int(request.args.get('contest_id'))
        ret = models.Exam.query.filter_by(id=contest_id).first()
        if ret:
            return model_to_dict(ret), HTTP_OK
        else:
            return {"message": "该考试不存在"}, HTTP_NOT_FOUND

    @auth_role(AUTH_TEACHER)
    def delete(self, contest_id):
        if request.method == 'OPTIONS':
            return '', 200
        exam = models.Exam.query.filter_by(id=contest_id).first()
        if not exam:
            return {"message": "该考试不存在"}, HTTP_NOT_FOUND
        models.ExamQuestion.query.filter_by(exam_id=contest_id).delete()
        models.ExamStudent.query.filter_by(exam_id=contest_id).delete()
        models.Submission.query.filter_by(exam_id=contest_id).delete()
        db.session.delete(exam)
        db.session.commit()
        return {}, HTTP_OK

    @auth_role(AUTH_TEACHER)
    def post(self):
        c = models.Exam()
        c.name = request.json.get('name', '')
        c.teacher_id = int(request.json.get('teacher_id'))
        c.start_time = request.json.get('start_time')
        c.end_time = request.json.get('end_time')
        if not (c.name and c.teacher_id and c.start_time and c.end_time):
            return {"message": "考试信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        db.session.add(c)
        db.session.commit()
        return {"message": "新增考试成功", "id": c.id}, HTTP_CREATED


class ContestList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        current_user_id = int(request.args.get('user_id'))
        current_user_role = int(request.args.get('user_role'))
        if current_user_role == AUTH_STUDENT:
            student_exams = models.ExamStudent.query.filter_by(student_id=current_user_id).all()
            exam_ids = [exam.exam_id for exam in student_exams]
            contests = models.Exam.query.filter(models.Exam.id.in_(exam_ids)).all()
        elif current_user_role == AUTH_ASSISTANT:
            student_ids = db.session.query(models.AssistantStudent.student_id).filter_by(assistant_id=current_user_id).all()
            student_ids = [s[0] for s in student_ids]
            exam_ids = db.session.query(models.ExamStudent.exam_id).filter(models.ExamStudent.student_id.in_(student_ids)).distinct().all()
            exam_ids = [e[0] for e in exam_ids]
            contests = models.Exam.query.filter(models.Exam.id.in_(exam_ids)).all()
        else:
            contests = models.Exam.query.all()
        data = [model_to_dict(contest) for contest in contests]
        return jsonify(data)


class ContestQuestion(Resource):
    def get(self):
        exam_id = int(request.args.get('contest_id'))
        exam_questions = models.ExamQuestion.query.filter_by(exam_id=exam_id).all()
        if not exam_questions:
            return jsonify({'message': 'No questions found for this exam.'}), 404
        question_ids = [eq.question_id for eq in exam_questions]
        return jsonify({'questionIds': question_ids})

    @auth_role(AUTH_TEACHER)
    def post(self):
        exam_id = int(request.json.get('exam_id'))
        question_id = int(request.json.get('question_id'))
        score = int(request.json.get('score'))
        if not (exam_id and question_id and score):
            return {"message": "信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        exam_question = models.ExamQuestion(exam_id=exam_id, question_id=question_id, score=score)
        db.session.add(exam_question)
        db.session.commit()
        return {"message": "考试-题目关联成功"}, HTTP_CREATED


class ContestStudent(Resource):
    @auth_role(AUTH_TEACHER)
    def post(self):
        exam_id = int(request.json.get('exam_id'))
        student_id = int(request.json.get('student_id'))
        if not (exam_id and student_id):
            return {"message": "信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        exam_student = models.ExamStudent(exam_id=exam_id, student_id=student_id, score=0)
        db.session.add(exam_student)
        db.session.commit()
        return {"message": "考试-学生关联成功"}, HTTP_CREATED

    @auth_role(AUTH_TEACHER)
    def get(self):
        student_id = int(request.args.get('userId'))
        if not student_id:
            return {"message": "缺少userId参数"}, HTTP_BAD_REQUEST
        exam_students = models.ExamStudent.query.filter_by(student_id=student_id).all()
        if not exam_students:
            return {"message": "该学生没有关联的考试"}, HTTP_NOT_FOUND
        exam_ids = [exam_student.exam_id for exam_student in exam_students]
        return {"exam_ids": exam_ids}, HTTP_OK


class ContestScores(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        contest_id = request.args.get('contest_id')
        user_id = request.args.get('user_id')
        user_role = request.args.get('user_role')
        if not contest_id:
            return {"message": "缺少考试ID"}, 400
        try:
            contest_id = int(contest_id)
        except ValueError:
            return {"message": "考试ID无效"}, 400
        if user_role is not None and int(user_role) == AUTH_ASSISTANT and user_id is not None:
            student_ids = [link.student_id for link in models.AssistantStudent.query.filter_by(assistant_id=int(user_id)).all()]
            exam_students = models.ExamStudent.query.filter(
                models.ExamStudent.exam_id == contest_id,
                models.ExamStudent.student_id.in_(student_ids)
            ).order_by(models.ExamStudent.score.desc()).all()
        else:
            exam_students = models.ExamStudent.query.filter_by(exam_id=contest_id).order_by(models.ExamStudent.score.desc()).all()
        if not exam_students:
            return {"message": "未找到相关成绩"}, 404
        student_scores = [{"id": es.student_id, "score": es.score, "rank": i + 1} for i, es in enumerate(exam_students)]
        return student_scores, 200


class GetScore(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        exam_id = int(request.args.get('exam_id'))
        student_id = int(request.args.get('student_id'))
        if not (exam_id and student_id):
            return {"message": "信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        exam_student = models.ExamStudent.query.filter_by(exam_id=exam_id, student_id=student_id).first()
        if not exam_student:
            return {"message": "该学生没有关联的考试"}, HTTP_NOT_FOUND
        return {"score": exam_student.score}, HTTP_OK


class UpdateScoreAPI(Resource):
    @auth_role(AUTH_ALL)
    def post(self):
        data = request.get_json()
        user_id = data.get('user_id')
        exam_id = data.get('exam_id')
        question_id = data.get('question_id')
        old_rate = data.get('old_rate')
        new_rate = data.get('new_rate')
        if not (user_id and exam_id and question_id and old_rate is not None and new_rate is not None):
            return {"message": "参数不全"}, 400
        try:
            exam_student = models.ExamStudent.query.filter_by(exam_id=exam_id, student_id=user_id).first()
            if not exam_student:
                return {"message": "未找到对应的考试学生记录"}, 404
            exam_question = models.ExamQuestion.query.filter_by(exam_id=exam_id, question_id=question_id).first()
            if not exam_question:
                return {"message": "未找到对应的考试题目记录"}, 404
            question_score = exam_question.score
            old_total = exam_student.score
            new_total = old_total - old_rate * question_score + new_rate * question_score
            exam_student.score = new_total
            db.session.commit()
            return {"message": "得分更新成功", "new_score": new_total}, 200
        except SQLAlchemyError as e:
            db.session.rollback()
            return {"message": "数据库错误: {}".format(str(e))}, 500
