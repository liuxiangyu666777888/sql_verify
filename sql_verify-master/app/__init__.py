from flask import Flask
from flask_restful import Api
from flask_cors import CORS
from .config import db, CORS_ORIGIN, DEBUG


def create_app():
    app = Flask(__name__)
    app.config.from_object('app.config')
    db.init_app(app)

    CORS(app, resources={r"/api/*": {
        "origins": CORS_ORIGIN,
        "methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        "supports_credentials": True
    }})

    # ── Error handlers ──
    @app.errorhandler(400)
    def bad_request(_e):
        return {"message": "请求参数有误"}, 400

    @app.errorhandler(401)
    def unauthorized(_e):
        return {"message": "未登录或身份信息已过期"}, 401

    @app.errorhandler(403)
    def forbidden(_e):
        return {"message": "无权执行此操作"}, 403

    @app.errorhandler(404)
    def not_found(_e):
        return {"message": "请求的资源不存在"}, 404

    @app.errorhandler(405)
    def method_not_allowed(_e):
        return {"message": "不支持的请求方法"}, 405

    @app.errorhandler(500)
    def server_error(_e):
        return {"message": "服务器内部错误，请稍后重试"}, 500

    api = Api(app)

    @app.route('/')
    def h():
        return ''

    # ── Route registration ──
    from .resources.auth import Login, Register
    from .resources.users import ManageUsers, Student, StudentList, UpdateSettings
    from .resources.questions import Answer, Question, QuestionList, TestCase as TestCaseRes, CheckQuestions
    from .resources.exams import Contest, ContestList, ContestQuestion, ContestStudent, ContestScores, GetScore, UpdateScoreAPI
    from .resources.judge import Judge, Submit, SubmitList, StatusCount, AnsweredQuestions, CheckStudents
    from .resources.classes import ClassResource, ClassList, ClassStudent
    from .resources.community import Community, CommunityList
    from .resources.assistant import AssistantStudent, AssistantStudentList
    from .resources.dashboard import TeacherDashboard

    api.add_resource(Answer, '/api/answer')
    api.add_resource(AnsweredQuestions, '/api/answeredquestions')
    api.add_resource(ContestList, '/api/contestlist')
    api.add_resource(Contest, '/api/contest', '/api/contest/<int:contest_id>')
    api.add_resource(ContestQuestion, '/api/contest-question')
    api.add_resource(ContestStudent, '/api/contest-student')
    api.add_resource(Judge, '/api/judge')
    api.add_resource(Login, '/api/login')
    api.add_resource(ManageUsers, '/api/manageusers')
    api.add_resource(Question, '/api/question', '/api/question/<int:question_id>')
    api.add_resource(QuestionList, '/api/questionlist')
    api.add_resource(Register, '/api/register')
    api.add_resource(StatusCount, '/api/statuscount')
    api.add_resource(Student, '/api/student')
    api.add_resource(StudentList, '/api/studentlist')
    api.add_resource(Submit, '/api/submit')
    api.add_resource(SubmitList, '/api/submitlist')
    api.add_resource(TestCaseRes, '/api/testcase')
    api.add_resource(UpdateScoreAPI, '/api/updatescore')
    api.add_resource(GetScore, '/api/getscore')
    api.add_resource(TeacherDashboard, '/api/teacher-dashboard')
    api.add_resource(ContestScores, '/api/contestscores')
    api.add_resource(CheckQuestions, '/api/check-questions')
    api.add_resource(CheckStudents, '/api/check-students')
    api.add_resource(UpdateSettings, '/api/updateSettings')
    api.add_resource(AssistantStudent, '/api/assistantstudents')
    api.add_resource(ClassResource, '/api/class')
    api.add_resource(ClassList, '/api/classlist')
    api.add_resource(ClassStudent, '/api/class-student')
    api.add_resource(Community, '/api/community')
    api.add_resource(CommunityList, '/api/communitylist')
    api.add_resource(AssistantStudentList, '/api/assistantstudentlist')

    with app.app_context():
        db.create_all()

    return app
