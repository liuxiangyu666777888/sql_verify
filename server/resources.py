from flask import request, jsonify
from flask_restful import Resource
import models, time, hashlib, os, ast
from config import *
from permissions import auth_role
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError, TimeoutError, OperationalError
from sqlalchemy.orm import sessionmaker
from sqlalchemy import func
from datetime import datetime
from sqlalchemy import create_engine

def parse_iso_datetime(iso_str):
    dt = datetime.fromisoformat(iso_str.replace('Z', '+00:00'))
    return dt
def model_to_dict(obj):
    """
    将 SQLAlchemy 模型对象转换为字典，并将 datetime 对象转换为字符串。
    """
    def convert(value):
        if isinstance(value, datetime):
            return value.strftime("%Y-%m-%d %H:%M:%S")
        return value

    return {c.name: convert(getattr(obj, c.name)) for c in obj.__table__.columns}



# answer
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


class AnsweredQuestions(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        student_id = int(request.args.get('student_id'))
        my_submits = models.Submission.query.filter_by(student_id=student_id).with_entities(models.Submission.question_id).distinct().all()
        unique_questions = {submit.question_id for submit in my_submits}
        return jsonify(list(unique_questions))


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
    




# community
class Community(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        article_id = request.args.get('article_id')
        if not article_id:
            return {"message": "缺少article_id参数"}, HTTP_BAD_REQUEST

        article_id = int(article_id)
        ret = models.Article.query.filter_by(id=article_id).first()
        if ret:
            return model_to_dict(ret), HTTP_OK
        else:
            return {"message": "文章不存在"}, HTTP_NOT_FOUND
    
    @auth_role(AUTH_ALL)
    def post(self):
        data = request.get_json()
        if not data:
            return {"message": "无效的请求数据"}, HTTP_BAD_REQUEST

        try:
            user_id = int(data.get('user_id'))
        except ValueError:
            return {"message": "无效的用户ID"}, HTTP_BAD_REQUEST
        
        max_id = db.session.query(func.max(models.Article.id)).scalar()
        max_id = max_id + 1 if max_id else 1
        article = models.Article(
            id=max_id,
            title=data.get('title'),
            content=data.get('content'),
            user_id=user_id,
            question_id=int(data.get('question_id')) if data.get('question_id') else None,
            is_notice=data.get('is_notice', False),
            publish_time=parse_iso_datetime(data.get('publish_time')),
            last_modify_time=parse_iso_datetime(data.get('last_modify_time'))
        )

        if article.title and article.content:
            db.session.add(article)
            db.session.commit()
            return {}, HTTP_CREATED
        else:
            return {"message": "文章缺少标题或内容！"}, HTTP_BAD_REQUEST
    
    @auth_role(AUTH_ALL)
    def put(self):
        data = request.get_json()
        article_id = data.get('article_id')
        if not article_id:
            return {"message": "缺少article_id参数"}, HTTP_BAD_REQUEST

        article_id = int(article_id)
        article = models.Article.query.filter_by(id=article_id).first()
        if not article:
            return {"message": "未找到文章！"}, HTTP_NOT_FOUND

        role = data.get('role', AUTH_STUDENT)
        user_id = data.get('user_id')
        if role == AUTH_STUDENT and article.user_id != user_id:
            return {"message": "没有编辑权限！"}, HTTP_FORBIDDEN
        
        new_content = data.get('new_content')
        if new_content:
            article.content = new_content
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "缺少新的内容"}, HTTP_BAD_REQUEST
    
    @auth_role(AUTH_ADMIN)
    def delete(self):
        article_id = request.args.get('article_id')
        if not article_id:
            return {"message": "缺少article_id参数"}, HTTP_BAD_REQUEST

        article_id = int(article_id)
        article = models.Article.query.filter_by(id=article_id).first()
        if article:
            db.session.delete(article)
            db.session.commit()
            return {}, HTTP_OK
        else:
            return {"message": "未找到文章！"}, HTTP_NOT_FOUND

# 文章列表类
class CommunityList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        user_id = int(request.args.get('user_id')) if request.args.get('user_id') else None
        articles = models.Article.query.all()
        if user_id:
            articles = models.Article.query.filter_by(user_id=user_id)
        else:
            articles = models.Article.query.all()
        data = [model_to_dict(article) for article in articles]
        return jsonify(data)


# contest
# 处理单个考试的相关功能
class Contest(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        # 查询单个考试 -> 用于考试查询和显示
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

        # 先删除 ExamQuestion 等依赖表记录
        models.ExamQuestion.query.filter_by(exam_id=contest_id).delete()
        models.ExamStudent.query.filter_by(exam_id=contest_id).delete()
        models.Submission.query.filter_by(exam_id=contest_id).delete()

    # 然后删除考试本身
        db.session.delete(exam)
        db.session.commit()
        return {}, HTTP_OK
    
    @auth_role(AUTH_TEACHER)
    def post(self):
        c = models.Exam()
        c.teacher_id = int(request.json.get('teacher_id'))
        c.start_time = request.json.get('start_time')
        c.end_time = request.json.get('end_time')

        if not (c.teacher_id and c.start_time and c.end_time):
            return {"message": "考试信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        db.session.add(c)
        db.session.commit()
        return {"message": "新增考试成功", "id": c.id}, HTTP_CREATED
    
class ContestList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        # 获取当前用户ID和角色
        current_user_id = int(request.args.get('user_id'))
        current_user_role = int(request.args.get('user_role'))

        if current_user_role == AUTH_STUDENT:
            # ✅ 学生：获取该学生参与的考试
            student_exams = models.ExamStudent.query.filter_by(student_id=current_user_id).all()
            exam_ids = [exam.exam_id for exam in student_exams]
            contests = models.Exam.query.filter(models.Exam.id.in_(exam_ids)).all()

        elif current_user_role == AUTH_ASSISTANT:
            # ✅ 助教：获取其被分配负责的学生考试
            student_ids = db.session.query(models.AssistantStudent.student_id).filter_by(assistant_id=current_user_id).all()
            student_ids = [s[0] for s in student_ids]
            exam_ids = db.session.query(models.ExamStudent.exam_id).filter(models.ExamStudent.student_id.in_(student_ids)).distinct().all()
            exam_ids = [e[0] for e in exam_ids]
            contests = models.Exam.query.filter(models.Exam.id.in_(exam_ids)).all()

        else:
            # ✅ 教师 / 管理员：返回所有考试
            contests = models.Exam.query.all()

        data = [model_to_dict(contest) for contest in contests]
        return jsonify(data)

## 获取和考试对应的题目id
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
        
        exam_question = models.ExamQuestion(
            exam_id=exam_id,
            question_id=question_id,
            score=score
        )
        
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
        
        exam_student = models.ExamStudent(
            exam_id=exam_id,
            student_id=student_id,
            score=0  # 默认分数为0
        )
        
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

            link = models.AssistantStudent(
                assistant_id=assistant_id,
                student_id=student_id
            )
            db.session.add(link)
        db.session.commit()
        return {"message": "分配成功"}, HTTP_CREATED
    
    @auth_role(AUTH_ASSISTANT)
    def get(self):
        assistant_id = request.args.get("assistant_id")

        if not assistant_id:
            return {"message": "缺少 assistant_id"}, 400

        # 查询分配给该助教的所有学生ID
        links = models.AssistantStudent.query.filter_by(assistant_id=assistant_id).all()
        student_ids = [link.student_id for link in links]

        # 获取学生基本信息
        students = models.User.query.filter(models.User.id.in_(student_ids)).all()

        result = []
        for s in students:
            result.append({
                "student_id": s.id,
                "username": s.username
            })

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

        # 查询所有被该助教管理的学生（通过 AssistantStudent 表）
        links = AssistantStudent.query.filter_by(assistant_id=assistant_id).all()
        student_ids = [link.student_id for link in links]

        # 再从 User 表中获取这些学生的详细信息
        students = models.User.query.filter(models.User.id.in_(student_ids)).all()
        result = [{
            "id": s.id,
            "username": s.username,
            "role": s.role
        } for s in students]

        return jsonify(result), HTTP_OK
    
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

        # 助教只能看自己分配的学生
        if user_role is not None and int(user_role) == AUTH_ASSISTANT and user_id is not None:
            # 获取该助教分配到的学生ID
            student_ids = [link.student_id for link in models.AssistantStudent.query.filter_by(assistant_id=int(user_id)).all()]
            exam_students = models.ExamStudent.query.filter(
                models.ExamStudent.exam_id == contest_id,
                models.ExamStudent.student_id.in_(student_ids)
            ).order_by(models.ExamStudent.score.desc()).all()
        else:
            # 其他角色可看全部
            exam_students = models.ExamStudent.query.filter_by(exam_id=contest_id).order_by(models.ExamStudent.score.desc()).all()
        
        if not exam_students:
            return {"message": "未找到相关成绩"}, 404

        student_scores = [
            {
                "id": exam_student.student_id,
                "score": exam_student.score,
                "rank": index + 1
            }
            for index, exam_student in enumerate(exam_students)
        ]

        return student_scores, 200



class GetScore(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        exam_id = int(request.args.get('exam_id'))
        student_id = int(request.args.get('student_id'))

        if not (exam_id and student_id):
            print(1)
            return {"message": "信息不全，补全缺失项！"}, HTTP_BAD_REQUEST
        
        exam_student = models.ExamStudent.query.filter_by(exam_id=exam_id, student_id=student_id).first()
        if not exam_student:
            print(2)
            return {"message": "该学生没有关联的考试"}, HTTP_NOT_FOUND
        
        score = exam_student.score
        print(score)
        return {"score": score}, HTTP_OK





# judge
# 定义返回结果的字段

ip_address = 'mysql+pymysql://root:7511881@localhost/test'
ip_address_no_db = 'mysql+pymysql://root:7511881@localhost'

class Judge(Resource):
    def execute_sql(self, code):
        """
        运行SQL代码，并捕获可能的错误和异常
        :param code: 要执行的SQL代码
        :return: 执行结果，格式为 (error: bool, msg: str)
        """
        # 创建数据库引擎，连接到test数据库
        engine = create_engine(ip_address)
        Session = sessionmaker(bind=engine)
        session = Session()

        try:
            session.begin()  # 开始事务
            start_time = time.time()  # 记录开始时间
            
            for stmt in code.split(';'):
                stmt = stmt.strip()
                if stmt:
                    result = session.execute(text(stmt))
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

        except OperationalError as e:  # 捕获操作错误异常
            session.rollback()  # 回滚事务
            return (True, str(e))  # 返回错误信息
        except TimeoutError:  # 捕获超时异常
            session.rollback()  # 回滚事务
            return (True, "TLE")  # 返回超时错误信息
        except SQLAlchemyError as e:  # 捕获SQLAlchemy异常
            session.rollback()  # 回滚事务
            return (True, str(e))  # 返回错误信息
        finally:
            session.close()  # 关闭会话
    def drop_database(self):
        """
        删除指定的表，避免数据干扰
        :param tablename: 需要删除的表名（可能有多个表名，以逗号分隔）
        """
        # 创建数据库引擎，连接到MySQL服务器（不指定数据库）
        engine = create_engine(ip_address_no_db)
        Session = sessionmaker(bind=engine)
        session = Session()

        try:
            session.begin()  # 开始事务
            session.execute(text(f'DROP DATABASE IF EXISTS test;'))
            session.commit()  # 提交事务
        except SQLAlchemyError:  # 捕获SQLAlchemy异常
            session.rollback()  # 回滚事务
        finally:
            session.close()  # 关闭会话

    def create_database(self):
        # 创建数据库引擎，连接到MySQL服务器（不指定数据库）
        engine = create_engine(ip_address_no_db)
        Session = sessionmaker(bind=engine)
        session = Session()

        try:
            session.begin()  # 开始事务
            session.execute(text(f"CREATE DATABASE IF NOT EXISTS test;"))
            session.commit()  # 提交事务
        except SQLAlchemyError:  # 捕获SQLAlchemy异常
            session.rollback()  # 回滚事务
        finally:
            session.close()  # 关闭会话

    def post(self):
        data = request.get_json()
        submit_sql = data.get('submit_sql')
        question_id = data.get('question_id')
        create_code = data.get('create_code')
        if not submit_sql or not question_id:
            return {"message": "提交信息不全"}, HTTP_BAD_REQUEST

        question_id = int(question_id)
        test_cases = models.TestCase.query.filter_by(question_id=question_id).all()
        results = {}

        for test_case in test_cases:
            test_id = test_case.id
            input_sql = str(test_case.input_sql)
            expected_output = ast.literal_eval(test_case.output)
            self.drop_database()
            self.create_database()

            fixed_create_code = "USE test;\n" + create_code if not create_code.strip().lower().startswith("use test") else create_code
            error, _ = self.execute_sql(fixed_create_code)
            error, _ = self.execute_sql(input_sql)
            # if error:
            #     results[test_id] = (True, JUDGE_RUNERROR)
            #     continue

            error, user_output = self.execute_sql(submit_sql)
            if error:
                if user_output == "TLE":
                    results[test_id] = (False, JUDGE_TIMELIMIT_EXCEED)
                elif "MemoryError" in user_output:
                    results[test_id] = (False, JUDGE_MEMLIMIT_EXCEED)
                else:
                    # Log the actual error message for debugging
                    print(f"Runtime error in test case {test_id}: {user_output}")
                    results[test_id] = (False, JUDGE_RUNERROR)
            elif user_output != expected_output:
                results[test_id] = (False, JUDGE_WRONGANSWER)
                print(f"Wrong answer in test case {test_id}: expected {expected_output}, got {user_output}")
            else:
                results[test_id] = (True, JUDGE_ACCEPTED)
        # 之后在此更新submit表的信息
        submit_id = int(data.get('submit_id')) if data.get('submit_id') else None
        record = models.Submission.query.filter_by(id=submit_id).first()
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
            record.status = JUDGE_ACCEPTED
        else:
            finalresult[1] = result_map[min(error_list)]
            pass_rate = (1.0 - len(error_list) * 1.0 / len(results))
            record.status = min(error_list)

        record.pass_rate = pass_rate
        db.session.commit()
        return {"result": tuple(finalresult), 'pass_rate': pass_rate}, HTTP_OK
    




# login
class Login(Resource):
    def post(self):
        data = request.get_json()
        user_id = data.get('id')
        password = data.get('password')
        role_str = data.get('role')
        
        if not user_id or not password  or not role_str:
            return {"message": "缺少用户名，密码或身份"}, HTTP_BAD_REQUEST

        role_map = {
            "student": 0,
            "teacher": 1,
            "admin": 2,
            "assistant": 3
        }

        if role_str not in role_map:
            return {"message": "非法身份信息"}, HTTP_BAD_REQUEST

        role = role_map[role_str]

        try:
            user_id = int(user_id)
        except ValueError:
            return {"message": "用户ID须为整数"}, HTTP_BAD_REQUEST

        # 查询用户
        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "用户名或密码无效"}, HTTP_UNAUTHORIZED

        # 校验密码
        hashed = hashlib.sha256(password.encode('utf8')).hexdigest()
        if user.password != hashed:
            return {"message": "用户名或密码无效"}, HTTP_UNAUTHORIZED

        # 校验身份
        if user.role != role:
            return {"message": "用户身份不匹配，请重新选择！"}, HTTP_UNAUTHORIZED

        # 生成并保存 session token
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





# manageUsers
class ManageUsers(Resource):
    def get(self):
        # Retrieve all users
        users = models.User.query.all()
        data = [model_to_dict(user) for user in users]
        return jsonify(data)

    def post(self):
        # Delete a user (legacy, if still used)
        user_id = int(request.json.get('id'))
        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "未找到用户。"}, HTTP_NOT_FOUND
        db.session.delete(user)
        db.session.commit()
        return {}, HTTP_OK

    def put(self):
        # Update user role
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
        # Delete user and related records
        user_id = request.args.get('user_id')
        try:
            uid = int(user_id)
        except (TypeError, ValueError):
            return {"message": "user_id 必须是整数。"}, HTTP_BAD_REQUEST

        user = models.User.query.filter_by(id=uid).first()
        if not user:
            return {"message": "未找到用户。"}, HTTP_NOT_FOUND

        # 删除考试-学生关联
        for es in models.ExamStudent.query.filter_by(student_id=uid).all():
            db.session.delete(es)
        # 删除助教-学生关联
        for eas in models.AssistantStudent.query.filter_by(assistant_id=uid).all():
            db.session.delete(eas)
        for eas in models.AssistantStudent.query.filter_by(student_id=uid).all():
            db.session.delete(eas)
        # 删除提交记录
        for sub in models.Submission.query.filter_by(student_id=uid).all():
            db.session.delete(sub)
        # 删除文章
        for art in models.Article.query.filter_by(user_id=uid).all():
            db.session.delete(art)
        # 删除老师创建的考试
        for ex in models.Exam.query.filter_by(teacher_id=uid).all():
            db.session.delete(ex)
        # 最后删除用户
        db.session.delete(user)
        db.session.commit()
        return {"message": "删除成功。"}, HTTP_OK



class StatusCount(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        student_id = int(request.args.get('student_id'))
        pass_count = models.Submission.query.filter_by(student_id=student_id, status=0).distinct(models.Submission.question_id).count()
        status_count = []
        for i in range(5):
            status_count.append(models.Submission.query.filter_by(student_id=student_id, status=i).count())
        return jsonify({'pass_count': pass_count, 'status_count': status_count})

# questions
# 处理单个题目的相关功能
class Question(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        # 查询单个题目 -> 用于题目查询和显示
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
            if len_users:
                completion_rate = int(10000 * len_submit_users / len_users) / 100.0
            else:
                completion_rate = 0.0
            if len_all_submits:
                accuracy = int(10000 * len_accepted_submits / len_all_submits) / 100.0
            else:
                accuracy = 0.0
            return dict(model_to_dict(ret), **{'accuracy' : accuracy, 
                                               'completion_rate': completion_rate,
                                               'completed': completed,
                                               'submission_count': submission_count}), HTTP_OK
        else:
            return {"message": "该题目不存在"}, HTTP_NOT_FOUND

    def delete(self, question_id):
        if request.method == 'OPTIONS':
            return '', 200  # 返回空响应并且允许跨域访问

        # 如果是 DELETE 请求，执行删除操作
        user_id = request.args.get('user_id')
        question = models.Question.query.get(question_id)
        if not question:
            return {"message": "该题目不存在"}, 404
        if question.teacher_id != int(user_id):
            return {"message": "无权删除他人发布的题目"}, 403

        try:
            db.session.delete(question)
            db.session.commit()
            return {}, 200  # 删除成功
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

# 处理题目列表的相关功能
class QuestionList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        # 查询所有题目 -> 用于题目列表的查询和显示
        student_id = request.args.get('student_id')
        teacher_id = request.args.get('teacher_id')
        data = []

        if student_id:
            student_id = int(student_id)
            questions = models.Question.query.all()
        # 计算题目难度
            for question in questions:
                all_submits = models.Submission.query.filter_by(question_id=question.id)
                accepted_submits = all_submits.filter_by(status=0)
                len_all_submits = all_submits.count()
                len_accepted_submits = accepted_submits.count()

                if len_all_submits:
                    accuracy = int(10000 * len_accepted_submits / len_all_submits) / 100.0
                else:
                    accuracy = 0.0
                ac = accepted_submits.filter_by(student_id=student_id).first() is not None
                data.append(dict(model_to_dict(question), **{'accuracy' : accuracy, 'AC' : ac}))
        elif teacher_id:
            teacher_id = int(teacher_id)
            questions = models.Question.query.filter_by(teacher_id=teacher_id).all()
            for question in questions:
                data.append(model_to_dict(question))
        return jsonify(data)




# register
class Register(Resource):
    def post(self):
        id = int(request.json.get('id')) if request.json.get('id') else None
        username = request.json.get('username', None)
        password = hashlib.sha256(request.json.get('password', 0).encode('utf8')).hexdigest()  
        role_str = request.json.get('role', 'student')
        ROLE_MAP = {
            "student": AUTH_STUDENT,
            "teacher": AUTH_TEACHER,
            "assistant": AUTH_ASSISTANT,
            "admin": AUTH_ADMIN
        }
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

        # 根据身份返回不同提示
        if role == AUTH_TEACHER:
            msg = "注册成功，欢迎加入 SQL 在线测评平台（教师端）。"
        elif role == AUTH_ASSISTANT:
            msg = "注册成功，欢迎使用 SQL 在线测评平台（助教权限已开启）。"
        else:
            msg = "注册成功，欢迎进入 SQL 在线测评平台，开始你的学习之旅！"

        return {"message": msg}, HTTP_CREATED


# students
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
    
    # TODO: 添加修改学生信息的函数

class StudentList(Resource):
    @auth_role(AUTH_ALL)
    def get(self):
        students = models.User.query.filter_by(role=AUTH_STUDENT).all()

        data = []
        for student in students:
            student_dict = model_to_dict(student)

            # 查询是否已分配助教
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




# setting
class UpdateSettings(Resource):
    def post(self):
        # 从请求中获取数据
        data = request.get_json()
        user_id = data.get('id')  # 用户的唯一标识符
        new_username = data.get('username')
        new_password = data.get('password')

        if not user_id:
            return {"message": "用户ID不能为空"}, 400
        
        # 查询用户是否存在
        user = models.User.query.filter_by(id=user_id).first()
        if not user:
            return {"message": "用户不存在"}, 404

        # 如果提供了新的用户名，检查用户名是否已经存在
        if new_username:
            existing_user = models.User.query.filter_by(username=new_username).first()
            if existing_user:
                return {"message": "该用户名已被注册，请选择其他用户名"}, 409
            user.username = new_username  # 更新用户名

        # 如果提供了新的密码，进行密码加密后更新
        if new_password:
            hashed_password = hashlib.sha256(new_password.encode('utf8')).hexdigest()
            user.password = hashed_password  # 更新密码

        # 提交数据库更改
        db.session.commit()

        return {"message": "设置已成功更新", "username": user.username, "password": new_password}, 200


# submit
class Submit(Resource):
    # 删除提交信息
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
    
    # 提交 - auth all
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

        # 同时返回submit的id
        return {"message": "提交成功", "submit_id": s.id}, HTTP_CREATED
    

    # 根据 question_id, exam_id, user_id获取提交信息
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
        fetchall = True if data.get('fetchall') == 'true' else False
        userid = int(data.get('user_id')) if data.get('user_id') else None
        if fetchall:
            submits = models.Submission.query.filter_by()
        elif userid:
            submits = models.Submission.query.filter_by(student_id=userid)
        else:
            return {"message": "学生不存在！"}, HTTP_BAD_REQUEST
        data = [model_to_dict(submit) for submit in submits]
        return jsonify(data)
    

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
            # 获取对应的 ExamStudent 项
            exam_student = models.ExamStudent.query.filter_by(exam_id=exam_id, student_id=user_id).first()
            if not exam_student:
                return {"message": "未找到对应的考试学生记录"}, 404

            # 获取对应的 ExamQuestion 项
            exam_question = models.ExamQuestion.query.filter_by(exam_id=exam_id, question_id=question_id).first()
            if not exam_question:
                return {"message": "未找到对应的考试题目记录"}, 404

            # 计算新的总分
            question_score = exam_question.score
            old_total = exam_student.score
            new_total = old_total - old_rate * question_score + new_rate * question_score

            # 更新总分
            exam_student.score = new_total
            db.session.commit()

            return {"message": "得分更新成功", "new_score": new_total}, 200
        except SQLAlchemyError as e:
            db.session.rollback()
            return {"message": "数据库错误: {}".format(str(e))}, 500