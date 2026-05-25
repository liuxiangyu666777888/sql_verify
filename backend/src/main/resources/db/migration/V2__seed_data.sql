USE sql_exam;

INSERT INTO users (user_id, username, password_hash, real_name, email, role, status)
VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36y3ThpLS2mA7Szdq1e6i6G', 'System Admin', 'admin@example.com', 'ADMIN', 'ACTIVE'),
(2, 'teacher1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36y3ThpLS2mA7Szdq1e6i6G', 'Teacher One', 'teacher1@example.com', 'TEACHER', 'ACTIVE'),
(3, 'student1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36y3ThpLS2mA7Szdq1e6i6G', 'Student One', 'student1@example.com', 'STUDENT', 'ACTIVE')
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO classes (class_id, class_name, teacher_id, semester, invite_code)
VALUES (1, '数据库课程一班', 2, '2025-2026-2', 'DB2026A')
ON DUPLICATE KEY UPDATE class_name = VALUES(class_name);

INSERT INTO student_class (student_id, class_id, status)
VALUES (3, 1, 'ACTIVE')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO questions (question_id, title, description, difficulty, answer_sql, creator_id, source_schema_sql, tags, visible)
VALUES (
  1,
  '部门工资最高的员工',
  '编写一个 SQL 查询，找出每个部门工资最高的员工。',
  'MEDIUM',
  'SELECT d.name AS Department, e.name AS Employee, e.salary AS Salary FROM Employee e JOIN Department d ON e.departmentId = d.id WHERE (e.departmentId, e.salary) IN (SELECT departmentId, MAX(salary) FROM Employee GROUP BY departmentId);',
  2,
  'CREATE TABLE Employee (id INT, name VARCHAR(20), salary INT, departmentId INT); CREATE TABLE Department (id INT, name VARCHAR(20));',
  JSON_ARRAY('JOIN', 'GROUP BY'),
  1
)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO test_cases (question_id, input_sql, expected_output, case_order, is_hidden)
VALUES (
  1,
  'CREATE TABLE Department (id INT, name VARCHAR(20)); INSERT INTO Department VALUES (1, ''IT''), (2, ''Sales''); CREATE TABLE Employee (id INT, name VARCHAR(20), salary INT, departmentId INT); INSERT INTO Employee VALUES (1, ''Joe'', 70000, 1), (2, ''Jim'', 90000, 1), (3, ''Henry'', 80000, 2), (4, ''Sam'', 60000, 2), (5, ''Max'', 90000, 1);',
  JSON_OBJECT('columns', JSON_ARRAY('Department', 'Employee', 'Salary'), 'rows', JSON_ARRAY(JSON_ARRAY('IT', 'Jim', 90000), JSON_ARRAY('Sales', 'Henry', 80000), JSON_ARRAY('IT', 'Max', 90000)), 'orderSensitive', false),
  1,
  0
)
ON DUPLICATE KEY UPDATE expected_output = VALUES(expected_output);

INSERT INTO exams (exam_id, exam_name, start_time, end_time, duration_minutes, instructions, lockdown_enabled, status, creator_id)
VALUES (1, 'CS304 Database Systems - Midterm', '2026-05-26 09:00:00', '2026-05-26 11:00:00', 120, '请使用标准 MySQL 语法完成题目。', 0, 'PUBLISHED', 2)
ON DUPLICATE KEY UPDATE exam_name = VALUES(exam_name);

INSERT INTO exam_questions (exam_id, question_id, score, question_order)
VALUES (1, 1, 100, 1);

INSERT INTO exam_students (exam_id, student_id, final_score, status)
VALUES (1, 3, 0, 'NOT_STARTED');
