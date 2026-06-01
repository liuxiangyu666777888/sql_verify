from sqlalchemy import ForeignKey, text, CheckConstraint, Column
from sqlalchemy.dialects.mysql import BIGINT, INTEGER, VARCHAR, FLOAT, TEXT, BOOLEAN, TIMESTAMP
from config import db

# 用户表
class User(db.Model):
    __tablename__ = 'User'
    id = Column(BIGINT, primary_key=True)  # 改为 BIGINT
    username = Column(VARCHAR(50), unique=True, nullable=False)
    password = Column(VARCHAR(255), nullable=False)
    role = Column(INTEGER, nullable=False)
    session = Column(VARCHAR(255), nullable=True)
    __table_args__ = (
        CheckConstraint('role IN (0, 1, 2, 3)', name='check_role'),
    )

# 题目表
class Question(db.Model):
    __tablename__ = 'Question'
    id = Column(INTEGER, primary_key=True, autoincrement=True)
    title = Column(VARCHAR(1000), nullable=False)
    create_code = Column(TEXT, nullable=False)
    description = Column(TEXT, nullable=False)
    input_example = Column(TEXT, nullable=True)
    output_example = Column(TEXT, nullable=True)
    difficulty = Column(INTEGER, nullable=False)
    answer_example = Column(TEXT, nullable=True)
    is_public = Column(BOOLEAN, nullable=False, default=True)
    teacher_id = Column(BIGINT, ForeignKey('User.id'), nullable=False)  # 改为 BIGINT
    teacher = db.relationship('User', backref='questions')


# 考试表
class Exam(db.Model):
    __tablename__ = 'Exam'
    id = Column(INTEGER, primary_key=True, autoincrement=True)
    teacher_id = Column(BIGINT, ForeignKey('User.id'))
    start_time = Column(TIMESTAMP, nullable=False)
    end_time = Column(TIMESTAMP, nullable=False)
    teacher = db.relationship('User', backref='exams')
    __table_args__ = (
        CheckConstraint('end_time > start_time', name='check_end_time'),
    )

# 考试-题目表
class ExamQuestion(db.Model):
    __tablename__ = 'Exam_Question'
    exam_id = Column(INTEGER, ForeignKey('Exam.id'), primary_key=True)
    question_id = Column(INTEGER, ForeignKey('Question.id'), primary_key=True)
    score = Column(INTEGER, nullable=False, default=10)
    exam = db.relationship('Exam', backref='exam_questions', passive_deletes=True)
    question = db.relationship('Question', backref='exam_questions')
    __table_args__ = (
        CheckConstraint('score > 0', name='check_score_positive'),
    )

# 考试-学生表
class ExamStudent(db.Model):
    __tablename__ = 'Exam_Student'
    # exam_id = -1:公共题目
    exam_id = Column(INTEGER, ForeignKey('Exam.id'), primary_key=True)
    student_id = Column(BIGINT, ForeignKey('User.id'), primary_key=True)
    score = Column(INTEGER, nullable=False, default=0)
    exam = db.relationship('Exam', backref='exam_students', passive_deletes=True)
    student = db.relationship('User', backref='exam_students')
    __table_args__ = (
        CheckConstraint('score >= 0', name='check_score_non_negative'),
    )

# 助教-学生对应表
class AssistantStudent(db.Model):
    __tablename__ = 'Assistant_Student'
    student_id = Column(BIGINT, ForeignKey('User.id'), primary_key=True)
    assistant_id = Column(BIGINT, ForeignKey('User.id'), nullable=False)

    student = db.relationship('User', foreign_keys=[student_id], backref='my_assistant')
    assistant = db.relationship('User', foreign_keys=[assistant_id], backref='my_students')

# 测试用例表
class TestCase(db.Model):
    __tablename__ = 'TestCase'
    id = Column(INTEGER, primary_key=True, autoincrement=True)
    tablename = Column(VARCHAR(1000), nullable=False)
    question_id = Column(INTEGER, ForeignKey('Question.id'))
    input_sql = Column(TEXT, nullable=False)
    output = Column(TEXT, nullable=False)
    question = db.relationship('Question', backref='test_cases')

# 提交表
class Submission(db.Model):
    __tablename__ = 'Submission'
    id = Column(INTEGER, primary_key=True, autoincrement=True)
    student_id = Column(BIGINT, ForeignKey('User.id'))
    exam_id = Column(INTEGER, ForeignKey('Exam.id'))
    question_id = Column(INTEGER, ForeignKey('Question.id'))
    submit_sql = Column(TEXT, nullable=False)
    submit_time = Column(TIMESTAMP, nullable=False, server_default=text('CURRENT_TIMESTAMP'))
    pass_rate = Column(FLOAT, nullable=False)
    # status 定义：
    '''
    -1: 等待判断题目
    0: Accepted
    1: RunError
    2. Wrong Answer
    3: TLE
    4: MLE
    '''
    status = Column(INTEGER, nullable=False)
    student = db.relationship('User', backref='submissions')
    exam = db.relationship('Exam', backref='submissions')
    question = db.relationship('Question', backref='submissions')
    __table_args__ = (
        CheckConstraint('pass_rate BETWEEN 0 AND 1', name='check_pass_rate'),
    )