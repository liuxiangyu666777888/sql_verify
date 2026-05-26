create database IF NOT EXISTS oj
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;
use oj;

-- 用户表 (User)
create table IF NOT EXISTS User (
    id bigint primary key,
    username varchar(50) not null unique,
    password varchar(255) not null,
    role int not null check (role in (0, 1, 2, 3)),
    session varchar(255),
    created_at timestamp not null default current_timestamp
);

-- 题目表 (Question)
create table IF NOT EXISTS Question (
    id int auto_increment primary key,
    title varchar(1000) not null,
    create_code text not null,
    description text not null,
    input_example text,
    output_example text,
    difficulty int not null,
    answer_example text,
    is_public boolean not null default true,
    teacher_id bigint not null,
    foreign key (teacher_id) references User(id)
);

-- 考试表 (Exam)
create table IF NOT EXISTS Exam (
    id int auto_increment primary key,
    name varchar(200) not null default '',
    teacher_id bigint,
    start_time timestamp not null,
    end_time timestamp not null,
    foreign key (teacher_id) references User(id),
    check (end_time > start_time)
);

-- 考试-题目表 (Exam_Question)
create table IF NOT EXISTS Exam_Question (
    exam_id int,
    question_id int,
    score int not null default 10 check (score > 0),
    primary key (exam_id, question_id),
    foreign key (exam_id) references Exam(id) on delete cascade,
    foreign key (question_id) references Question(id) on delete cascade
);

-- 考试-学生表 (Exam_Student)
create table IF NOT EXISTS Exam_Student (
    exam_id int,
    student_id bigint,
    score int not null default 0 check (score >= 0),
    primary key (exam_id, student_id),
    foreign key (exam_id) references Exam(id) on delete cascade,
    foreign key (student_id) references User(id)
);

-- 助教-学生对应表 (Assistant_Student)
CREATE TABLE IF NOT EXISTS Assistant_Student (
    assistant_id BIGINT,
    student_id BIGINT PRIMARY KEY,
    FOREIGN KEY (assistant_id) REFERENCES User(id),
    FOREIGN KEY (student_id) REFERENCES User(id)
);


-- 测试用例表 (TestCase)
create table IF NOT EXISTS TestCase (
    id int auto_increment primary key,
    tablename varchar(1000) not null,
    question_id int,
    input_sql text not null,
    output text not null,
    foreign key (question_id) references Question(id)
);

-- 班级表 (Class)
create table IF NOT EXISTS Class (
    id int auto_increment primary key,
    name varchar(100) not null,
    teacher_id bigint not null,
    foreign key (teacher_id) references User(id)
);

-- 学生-班级关联表 (Student_Class)
create table IF NOT EXISTS Student_Class (
    student_id bigint,
    class_id int,
    primary key (student_id, class_id),
    foreign key (student_id) references User(id),
    foreign key (class_id) references Class(id) on delete cascade
);

-- 社区文章表 (Article)
create table IF NOT EXISTS Article (
    id int auto_increment primary key,
    title varchar(255) not null,
    content text not null,
    user_id bigint not null,
    question_id int,
    is_notice boolean not null default false,
    publish_time timestamp not null default current_timestamp,
    last_modify_time timestamp not null default current_timestamp,
    foreign key (user_id) references User(id),
    foreign key (question_id) references Question(id)
);

-- 提交表 (Submission)
create table IF NOT EXISTS Submission (
    id int auto_increment primary key,
    student_id bigint,
    exam_id int,
    question_id int,
    submit_sql text not null,
    submit_time timestamp not null default current_timestamp,
    pass_rate float not null check (pass_rate between 0 and 1),
    status int not null,
    foreign key (student_id) references User(id),
    foreign key (exam_id) references Exam(id),
    foreign key (question_id) references Question(id)
);

-- 外键索引
CREATE INDEX IF NOT EXISTS idx_question_teacher ON Question(teacher_id);
CREATE INDEX IF NOT EXISTS idx_exam_teacher ON Exam(teacher_id);
CREATE INDEX IF NOT EXISTS idx_assistant_asst ON Assistant_Student(assistant_id);
CREATE INDEX IF NOT EXISTS idx_testcase_question ON TestCase(question_id);
CREATE INDEX IF NOT EXISTS idx_submission_student ON Submission(student_id);
CREATE INDEX IF NOT EXISTS idx_submission_exam ON Submission(exam_id);
CREATE INDEX IF NOT EXISTS idx_submission_question ON Submission(question_id);
CREATE INDEX IF NOT EXISTS idx_class_teacher ON Class(teacher_id);
CREATE INDEX IF NOT EXISTS idx_article_user ON Article(user_id);
CREATE INDEX IF NOT EXISTS idx_article_question ON Article(question_id);