from flask import request, jsonify
from flask_restful import Resource
from ..config import *
from .. import models
from ..permissions import auth_role
from . import model_to_dict


class Answer(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        question_id = request.args.get('question_id')
        if not question_id:
            return {"message": "缺少question_id参数"}, HTTP_BAD_REQUEST
        question_id = int(question_id)
        ret = models.Question.query.filter_by(id=question_id).first()
        if ret:
            return model_to_dict(ret), HTTP_OK
        else:
            return {"message": "该答案不存在"}, HTTP_NOT_FOUND

    @auth_role(AUTH_TEACHER)
    def delete(self):
        question_id = request.args.get('question_id')
        if not question_id:
            return {"message": "缺少question_id参数"}, HTTP_BAD_REQUEST
        question_id = int(question_id)
        ret = models.Question.query.filter_by(id=question_id).first()
        if ret:
            db.session.delete(ret)
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "该答案不存在"}, HTTP_NOT_FOUND


class Question(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        question_id = int(request.args.get('question_id'))
        ret = models.Question.query.filter_by(id=question_id).first()
        if ret:
            student_id = int(request.args.get('student_id'))
            all_submits = models.Submission.query.filter_by(question_id=question_id)
            submission_count = all_submits.filter_by(student_id=student_id).count()
            completed = all_submits.filter_by(student_id=student_id, status=0).first() is not None
            accepted_submits = all_submits.filter_by(status=0)
            len_all_submits = all_submits.count()
            len_accepted_submits = accepted_submits.count()
            len_submit_users = all_submits.with_entities(models.Submission.student_id).distinct().count()
            len_users = models.User.query.count()
            completion_rate = int(10000 * len_submit_users / len_users) / 100.0 if len_users else 0.0
            accuracy = int(10000 * len_accepted_submits / len_all_submits) / 100.0 if len_all_submits else 0.0
            return dict(model_to_dict(ret), **{
                'accuracy': accuracy,
                'completion_rate': completion_rate,
                'completed': completed,
                'submission_count': submission_count
            }), HTTP_OK
        else:
            return {"message": "该题目不存在"}, HTTP_NOT_FOUND

    def delete(self, question_id):
        if request.method == 'OPTIONS':
            return '', 200
        user_id = request.args.get('user_id')
        question = models.Question.query.get(question_id)
        if not question:
            return {"message": "该题目不存在"}, 404
        if question.teacher_id != int(user_id):
            return {"message": "无权删除他人发布的题目"}, 403
        try:
            db.session.delete(question)
            db.session.commit()
            return {}, 200
        except Exception as e:
            db.session.rollback()
            return {"message": "删除失败", "error": str(e)}, 500

    @auth_role(AUTH_TEACHER)
    def post(self):
        q = models.Question()
        q.title = request.json.get('title')
        q.description = request.json.get('description')
        q.create_code = request.json.get('create_code')
        q.difficulty = int(request.json.get('difficulty', 1))
        q.input_example = request.json.get('input_example', '')
        q.output_example = request.json.get('output_example', '')
        q.answer_example = request.json.get('answer_example', '')
        q.is_public = request.json.get('is_public', False)
        q.teacher_id = int(request.json.get('teacher_id'))
        if not (q.title and q.description and q.create_code):
            return {"message": "题目信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        db.session.add(q)
        db.session.commit()
        return {"message": "新增题目成功", 'question_id': q.id}, HTTP_CREATED


class QuestionList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        student_id = request.args.get('student_id')
        teacher_id = request.args.get('teacher_id')
        data = []
        if student_id:
            student_id = int(student_id)
            questions = models.Question.query.all()
            for question in questions:
                all_submits = models.Submission.query.filter_by(question_id=question.id)
                accepted_submits = all_submits.filter_by(status=0)
                len_all_submits = all_submits.count()
                len_accepted_submits = accepted_submits.count()
                accuracy = int(10000 * len_accepted_submits / len_all_submits) / 100.0 if len_all_submits else 0.0
                ac = accepted_submits.filter_by(student_id=student_id).first() is not None
                data.append(dict(model_to_dict(question), **{'accuracy': accuracy, 'AC': ac}))
        elif teacher_id:
            teacher_id = int(teacher_id)
            questions = models.Question.query.filter_by(teacher_id=teacher_id).all()
            for question in questions:
                data.append(model_to_dict(question))
        return jsonify(data)


class TestCase(Resource):
    @auth_role(AUTH_TEACHER)
    def post(self):
        test_cases = request.json.get('test_cases')
        question_id = request.json.get('question_id')
        if not test_cases or not question_id:
            return {"message": "测试点信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        for case in test_cases:
            tc = models.TestCase()
            tc.tablename = case.get('tablename')
            tc.input_sql = case.get('input_sql')
            tc.output = case.get('output')
            tc.question_id = question_id
            db.session.add(tc)
        db.session.commit()
        return {"message": "新增题目成功"}, HTTP_CREATED


class CheckQuestions(Resource):
    def post(self):
        data = request.get_json()
        question_ids = data.get('questionIds', [])
        invalid_ids = []
        for q_id in question_ids:
            question = models.Question.query.get(q_id)
            if not question:
                invalid_ids.append(q_id)
        return {"invalidIds": invalid_ids}, 200
