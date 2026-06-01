from flask import Flask
from flask_restful import Api
from flask_cors import CORS
import os, sys
sys.path.append(os.getcwd())
import config
from resources import *

def create_app():
    app = Flask(__name__)
    app.config.from_object(config)
    config.db.init_app(app)
    CORS(app, resources={r"/api/*": {"origins": "http://localhost:8080", "methods": ["GET", "POST", "DELETE", "OPTIONS"], "supports_credentials": True}})
    
    api = Api(app)
    
    @app.route('/')
    def h():
        return ''
    
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
    api.add_resource(TestCase, '/api/testcase')
    api.add_resource(UpdateScoreAPI, '/api/updatescore')
    api.add_resource(GetScore, '/api/getscore')
    api.add_resource(ContestScores, '/api/contestscores')
    api.add_resource(CheckQuestions, '/api/check-questions')
    api.add_resource(CheckStudents, '/api/check-students')
    api.add_resource(UpdateSettings, '/api/updateSettings')
    api.add_resource(AssistantStudent, '/api/assistantstudents')

    with app.app_context():
        config.db.create_all()
    return app

app = create_app()

if __name__ == '__main__':
    app.run(debug=True)
