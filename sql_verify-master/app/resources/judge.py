from flask import request, jsonify
from flask_restful import Resource
import time, ast
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError, TimeoutError, OperationalError
from sqlalchemy.orm import sessionmaker
from ..config import *
from .. import models
from ..permissions import auth_role
from . import model_to_dict, parse_iso_datetime


class Judge(Resource):
    def execute_sql(self, code):
        engine = create_engine(JUDGE_DB_URI)
        Session = sessionmaker(bind=engine)
        session = Session()
        try:
            session.begin()
            start_time = time.time()
            result = None
            for stmt in code.split(';'):
                stmt = stmt.strip()
                if stmt:
                    result = session.execute(text(stmt))
            if result is None:
                return (True, "No valid SQL statement")
            session.commit()
            elapsed_time = time.time() - start_time
            if elapsed_time > 5:
                return (True, "TLE")
            if result.returns_rows:
                output = result.fetchall()
                output = [dict(row) if isinstance(row, dict) else row for row in output]
            else:
                output = "No data"
            return (False, output)
        except OperationalError as e:
            session.rollback()
            return (True, str(e))
        except TimeoutError:
            session.rollback()
            return (True, "TLE")
        except SQLAlchemyError as e:
            session.rollback()
            return (True, str(e))
        finally:
            session.close()

    def ensure_test_database(self):
        engine = create_engine(JUDGE_DB_URI_NO_DB)
        Session = sessionmaker(bind=engine)
        session = Session()
        try:
            session.begin()
            session.execute(text("CREATE DATABASE IF NOT EXISTS test;"))
            session.commit()
        except SQLAlchemyError:
            session.rollback()
        finally:
            session.close()

    def clean_test_tables(self):
        engine = create_engine(JUDGE_DB_URI)
        Session = sessionmaker(bind=engine)
        session = Session()
        try:
            session.begin()
            session.execute(text("SET FOREIGN_KEY_CHECKS = 0;"))
            tables = session.execute(text("SHOW TABLES")).fetchall()
            for t in tables:
                session.execute(text(f"DROP TABLE IF EXISTS `{t[0]}`;"))
            session.execute(text("SET FOREIGN_KEY_CHECKS = 1;"))
            session.commit()
        except SQLAlchemyError:
            session.rollback()
        finally:
            session.close()

    def post(self):
        data = request.get_json()
        submit_sql = data.get('submit_sql')
        question_id = data.get('question_id')
        create_code = data.get('create_code')
        submit_id = int(data.get('submit_id')) if data.get('submit_id') else None

        if not submit_sql or not question_id:
            return {"message": "提交信息不全"}, HTTP_BAD_REQUEST

        question_id = int(question_id)

        if submit_id:
            record = models.Submission.query.filter_by(id=submit_id).first()
            if not record:
                return {"message": "提交记录不存在"}, HTTP_NOT_FOUND
        else:
            record = None

        if not create_code:
            question = models.Question.query.filter_by(id=question_id).first()
            if not question:
                return {"message": "题目不存在"}, HTTP_NOT_FOUND
            create_code = question.create_code

        test_cases = models.TestCase.query.filter_by(question_id=question_id).all()
        if not test_cases:
            return {"message": "该题目没有测试用例"}, HTTP_BAD_REQUEST

        self.ensure_test_database()
        results = {}

        for test_case in test_cases:
            test_id = test_case.id
            input_sql = str(test_case.input_sql)
            expected_output = ast.literal_eval(test_case.output)
            self.clean_test_tables()

            fixed_create_code = "USE test;\n" + create_code if not create_code.strip().lower().startswith("use test") else create_code
            self.execute_sql(fixed_create_code)
            self.execute_sql(input_sql)

            error, user_output = self.execute_sql(submit_sql)
            if error:
                if user_output == "TLE":
                    results[test_id] = (False, JUDGE_TIMELIMIT_EXCEED)
                elif "MemoryError" in user_output:
                    results[test_id] = (False, JUDGE_MEMLIMIT_EXCEED)
                else:
                    results[test_id] = (False, JUDGE_RUNERROR)
            elif user_output != expected_output:
                results[test_id] = (False, JUDGE_WRONGANSWER)
            else:
                results[test_id] = (True, JUDGE_ACCEPTED)

        finalresult = [True, 'Pending']
        error_list = []
        for _, result in results.items():
            if not result[0]:
                error_list.append(result[1])
                finalresult[0] = False

        result_map = {
            JUDGE_RUNERROR: 'Runtime error',
            JUDGE_WRONGANSWER: "Wrong answer",
            JUDGE_TIMELIMIT_EXCEED: "Time limit exceeded",
            JUDGE_MEMLIMIT_EXCEED: "Memory limit exceeded"
        }
        if finalresult[0]:
            finalresult[1] = 'Accepted'
            pass_rate = 1
            if record:
                record.status = JUDGE_ACCEPTED
        else:
            finalresult[1] = result_map[min(error_list)]
            pass_rate = (1.0 - len(error_list) * 1.0 / len(results))
            if record:
                record.status = min(error_list)

        if record:
            record.pass_rate = pass_rate
            db.session.commit()

        return {"result": tuple(finalresult), 'pass_rate': pass_rate}, HTTP_OK


class Submit(Resource):
    @auth_role(AUTH_ADMIN)
    def delete(self):
        submit_id = int(request.args.get("submit_id"))
        ret = models.Submission.query.filter_by(id=submit_id).first()
        if ret:
            db.session.delete(ret)
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "该提交记录不存在"}, HTTP_NOT_FOUND

    @auth_role(AUTH_ALL)
    def post(self):
        s = models.Submission()
        s.student_id = int(request.json.get('student_id'))
        s.question_id = int(request.json.get('question_id'))
        s.exam_id = int(request.json.get('exam_id')) if request.json.get('exam_id') else None
        s.submit_sql = request.json.get('submit_sql')
        s.submit_time = parse_iso_datetime(request.json.get('submit_time'))
        s.pass_rate = 0
        s.status = JUDGE_PENDING
        if not (s.student_id and s.question_id and s.submit_sql):
            return {"message": "提交信息不全"}, HTTP_BAD_REQUEST
        db.session.add(s)
        db.session.commit()
        return {"message": "提交成功", "submit_id": s.id}, HTTP_CREATED

    @auth_role(AUTH_ALL)
    def get(self):
        data = dict(request.args)
        user_id = int(data.get('student_id')) if data.get('student_id') else None
        question_id = int(data.get('question_id')) if data.get('question_id') else None
        exam_id = int(data.get('exam_id')) if data.get('exam_id') else None
        if user_id and question_id and exam_id:
            submits = models.Submission.query.filter_by(student_id=user_id, question_id=question_id, exam_id=exam_id).all()
            if not submits:
                return {"pass_rate": 0}, HTTP_OK
            else:
                max_pass_rate = max(submit.pass_rate for submit in submits)
                return {"pass_rate": max_pass_rate}, HTTP_OK
        else:
            return {"message": "提交信息不全"}, HTTP_BAD_REQUEST


class SubmitList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        data = dict(request.args)
        fetchall = data.get('fetchall') == 'true'
        user_id = int(data.get('user_id')) if data.get('user_id') else None
        exam_id = int(data.get('exam_id')) if data.get('exam_id') else None
        question_id = int(data.get('question_id')) if data.get('question_id') else None
        class_id = int(data.get('class_id')) if data.get('class_id') else None

        query = models.Submission.query

        if fetchall:
            pass
        elif class_id:
            student_ids = db.session.query(models.StudentClass.student_id).filter_by(class_id=class_id).all()
            ids = [s[0] for s in student_ids]
            query = query.filter(models.Submission.student_id.in_(ids)) if ids else query.filter(models.Submission.id == -1)
        elif exam_id:
            query = query.filter_by(exam_id=exam_id)
        elif question_id:
            query = query.filter_by(question_id=question_id)
        elif user_id:
            query = query.filter_by(student_id=user_id)
        else:
            return {"message": "请提供筛选条件"}, HTTP_BAD_REQUEST

        submits = query.order_by(models.Submission.submit_time.desc()).limit(200).all()
        result = []
        for s in submits:
            item = model_to_dict(s)
            item['student_name'] = s.student.username if s.student else None
            item['question_title'] = s.question.title if s.question else None
            result.append(item)
        return jsonify(result)


class StatusCount(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        student_id = int(request.args.get('student_id'))
        pass_count = models.Submission.query.filter_by(student_id=student_id, status=0).distinct(models.Submission.question_id).count()
        status_count = []
        for i in range(5):
            status_count.append(models.Submission.query.filter_by(student_id=student_id, status=i).count())
        return jsonify({'pass_count': pass_count, 'status_count': status_count})


class AnsweredQuestions(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        student_id = int(request.args.get('student_id'))
        my_submits = models.Submission.query.filter_by(student_id=student_id).with_entities(models.Submission.question_id).distinct().all()
        unique_questions = {submit.question_id for submit in my_submits}
        return jsonify(list(unique_questions))


class CheckStudents(Resource):
    def post(self):
        data = request.get_json()
        student_ids = data.get('studentIds', [])
        invalid_ids = []
        for s_id in student_ids:
            student = models.User.query.get(s_id)
            if not student:
                invalid_ids.append(s_id)
        return {"invalidIds": invalid_ids}, 200
