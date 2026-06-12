USE sql_exam;

INSERT INTO questions (question_id, title, description, difficulty, answer_sql, creator_id, source_schema_sql, tags, visible)
VALUES
(
  175,
  '175. 组合两个表',
  '表 Person 包含 PersonId、FirstName、LastName；表 Address 包含 AddressId、PersonId、City、State。编写一个 SQL 查询，无论 person 是否有地址信息，都需要返回 FirstName、LastName、City、State。',
  'EASY',
  'SELECT p.FirstName, p.LastName, a.City, a.State FROM Person p LEFT JOIN Address a ON a.PersonId = p.PersonId;',
  2,
  'CREATE TABLE Person (PersonId INT PRIMARY KEY, FirstName VARCHAR(50), LastName VARCHAR(50)); CREATE TABLE Address (AddressId INT PRIMARY KEY, PersonId INT, City VARCHAR(50), State VARCHAR(50));',
  JSON_ARRAY('LeetCode', 'JOIN', 'LEFT JOIN'),
  1
),
(
  176,
  '176. 第二高的薪水',
  'Employee 表包含 Id 和 Salary。编写一个 SQL 查询，返回第二高的薪水，列名为 SecondHighestSalary；如果不存在第二高的薪水，则返回 NULL。',
  'EASY',
  'SELECT IFNULL((SELECT DISTINCT Salary FROM Employee ORDER BY Salary DESC LIMIT 1 OFFSET 1), NULL) AS SecondHighestSalary;',
  2,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Salary INT);',
  JSON_ARRAY('LeetCode', 'ORDER BY', 'LIMIT'),
  1
),
(
  177,
  '177. 第 N 高的薪水（练习版 N=2）',
  'Employee 表包含 Id 和 Salary。原题要求编写函数 getNthHighestSalary(N)，但本平台当前只允许提交 SELECT 查询；本练习固定 N = 2，请返回第二高的薪水，列名为 getNthHighestSalary(2)。如果不存在第二高的薪水，则返回 NULL。',
  'MEDIUM',
  'SELECT IFNULL((SELECT Salary FROM (SELECT Salary, DENSE_RANK() OVER (ORDER BY Salary DESC) AS rk FROM Employee GROUP BY Salary) t WHERE rk = 2), NULL) AS `getNthHighestSalary(2)`;',
  2,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Salary INT);',
  JSON_ARRAY('LeetCode', 'WINDOW', 'DENSE_RANK'),
  1
),
(
  178,
  '178. 分数排名',
  'Scores 表包含 Id 和 Score。编写一个 SQL 查询来实现分数排名：相同分数排名相同，下一名次应连续不跳号。返回列为 Score、Rank。',
  'MEDIUM',
  'SELECT Score, DENSE_RANK() OVER (ORDER BY Score DESC) AS `Rank` FROM Scores;',
  2,
  'CREATE TABLE Scores (Id INT PRIMARY KEY, Score DECIMAL(4,2));',
  JSON_ARRAY('LeetCode', 'WINDOW', 'DENSE_RANK'),
  1
),
(
  180,
  '180. 连续出现的数字',
  'Logs 表包含 Id 和 Num。编写一个 SQL 查询，查找所有至少连续出现三次的数字，返回列名为 ConsecutiveNums。',
  'MEDIUM',
  'SELECT DISTINCT Num AS ConsecutiveNums FROM (SELECT Num, LEAD(Num, 1) OVER (ORDER BY Id) AS n2, LEAD(Num, 2) OVER (ORDER BY Id) AS n3 FROM Logs) t WHERE Num = n2 AND Num = n3;',
  2,
  'CREATE TABLE Logs (Id INT PRIMARY KEY, Num INT);',
  JSON_ARRAY('LeetCode', 'WINDOW', 'LEAD'),
  1
),
(
  181,
  '181. 超过经理收入的员工',
  'Employee 表包含员工 Id、Name、Salary 和 ManagerId。编写一个 SQL 查询，找出收入超过其经理的员工姓名，返回列名为 Employee。',
  'EASY',
  'SELECT e.Name AS Employee FROM Employee e JOIN Employee m ON e.ManagerId = m.Id WHERE e.Salary > m.Salary;',
  2,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Name VARCHAR(50), Salary INT, ManagerId INT NULL);',
  JSON_ARRAY('LeetCode', 'JOIN', 'SELF JOIN'),
  1
),
(
  182,
  '182. 查找重复的电子邮箱',
  'Person 表包含 Id 和 Email。编写一个 SQL 查询，查找所有重复的电子邮箱，返回列名为 Email。',
  'EASY',
  'SELECT Email FROM Person GROUP BY Email HAVING COUNT(*) > 1;',
  2,
  'CREATE TABLE Person (Id INT PRIMARY KEY, Email VARCHAR(100));',
  JSON_ARRAY('LeetCode', 'GROUP BY', 'HAVING'),
  1
),
(
  183,
  '183. 从不订购的客户',
  'Customers 表包含 Id 和 Name；Orders 表包含 Id 和 CustomerId。编写一个 SQL 查询，找出所有从不订购任何东西的客户，返回列名为 Customers。',
  'EASY',
  'SELECT c.Name AS Customers FROM Customers c LEFT JOIN Orders o ON c.Id = o.CustomerId WHERE o.Id IS NULL;',
  2,
  'CREATE TABLE Customers (Id INT PRIMARY KEY, Name VARCHAR(50)); CREATE TABLE Orders (Id INT PRIMARY KEY, CustomerId INT);',
  JSON_ARRAY('LeetCode', 'LEFT JOIN', 'NULL'),
  1
),
(
  184,
  '184. 部门工资最高的员工',
  'Employee 表包含 Id、Name、Salary、DepartmentId；Department 表包含 Id、Name。编写一个 SQL 查询，找出每个部门工资最高的员工，返回 Department、Employee、Salary。',
  'MEDIUM',
  'SELECT Department, Employee, Salary FROM (SELECT d.Name AS Department, e.Name AS Employee, e.Salary, RANK() OVER (PARTITION BY d.Id ORDER BY e.Salary DESC) AS rk FROM Employee e JOIN Department d ON e.DepartmentId = d.Id) t WHERE rk = 1;',
  2,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Name VARCHAR(50), Salary INT, DepartmentId INT); CREATE TABLE Department (Id INT PRIMARY KEY, Name VARCHAR(50));',
  JSON_ARRAY('LeetCode', 'JOIN', 'WINDOW', 'RANK'),
  1
),
(
  185,
  '185. 部门工资前三高的所有员工',
  'Employee 表包含 Id、Name、Salary、DepartmentId；Department 表包含 Id、Name。编写一个 SQL 查询，找出每个部门获得前三高工资的所有员工，返回 Department、Employee、Salary。',
  'HARD',
  'SELECT Department, Employee, Salary FROM (SELECT d.Name AS Department, e.Name AS Employee, e.Salary, DENSE_RANK() OVER (PARTITION BY d.Id ORDER BY e.Salary DESC) AS rk FROM Employee e JOIN Department d ON e.DepartmentId = d.Id) t WHERE rk <= 3;',
  2,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Name VARCHAR(50), Salary INT, DepartmentId INT); CREATE TABLE Department (Id INT PRIMARY KEY, Name VARCHAR(50));',
  JSON_ARRAY('LeetCode', 'JOIN', 'WINDOW', 'DENSE_RANK'),
  1
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  difficulty = VALUES(difficulty),
  answer_sql = VALUES(answer_sql),
  source_schema_sql = VALUES(source_schema_sql),
  tags = VALUES(tags),
  visible = VALUES(visible);

INSERT INTO test_cases (case_id, question_id, input_sql, expected_output, case_order, is_hidden)
VALUES
(
  17501,
  175,
  'CREATE TABLE Person (PersonId INT PRIMARY KEY, FirstName VARCHAR(50), LastName VARCHAR(50)); INSERT INTO Person VALUES (1, ''Allen'', ''Wang''), (2, ''Bob'', ''Alice''); CREATE TABLE Address (AddressId INT PRIMARY KEY, PersonId INT, City VARCHAR(50), State VARCHAR(50)); INSERT INTO Address VALUES (1, 2, ''New York City'', ''New York'');',
  JSON_OBJECT('columns', JSON_ARRAY('FirstName', 'LastName', 'City', 'State'), 'rows', JSON_ARRAY(JSON_ARRAY('Allen', 'Wang', CAST(NULL AS CHAR), CAST(NULL AS CHAR)), JSON_ARRAY('Bob', 'Alice', 'New York City', 'New York')), 'orderSensitive', false),
  1,
  0
),
(
  17601,
  176,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Salary INT); INSERT INTO Employee VALUES (1, 100), (2, 200), (3, 300);',
  JSON_OBJECT('columns', JSON_ARRAY('SecondHighestSalary'), 'rows', JSON_ARRAY(JSON_ARRAY(200)), 'orderSensitive', false),
  1,
  0
),
(
  17701,
  177,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Salary INT); INSERT INTO Employee VALUES (1, 100), (2, 200), (3, 300);',
  JSON_OBJECT('columns', JSON_ARRAY('getNthHighestSalary(2)'), 'rows', JSON_ARRAY(JSON_ARRAY(200)), 'orderSensitive', false),
  1,
  0
),
(
  17801,
  178,
  'CREATE TABLE Scores (Id INT PRIMARY KEY, Score DECIMAL(4,2)); INSERT INTO Scores VALUES (1, 3.50), (2, 3.65), (3, 4.00), (4, 3.85), (5, 4.00), (6, 3.65);',
  JSON_OBJECT('columns', JSON_ARRAY('Score', 'Rank'), 'rows', JSON_ARRAY(JSON_ARRAY(4, 1), JSON_ARRAY(4, 1), JSON_ARRAY(3.85, 2), JSON_ARRAY(3.65, 3), JSON_ARRAY(3.65, 3), JSON_ARRAY(3.5, 4)), 'orderSensitive', false),
  1,
  0
),
(
  18001,
  180,
  'CREATE TABLE Logs (Id INT PRIMARY KEY, Num INT); INSERT INTO Logs VALUES (1, 1), (2, 1), (3, 1), (4, 2), (5, 1), (6, 2), (7, 2);',
  JSON_OBJECT('columns', JSON_ARRAY('ConsecutiveNums'), 'rows', JSON_ARRAY(JSON_ARRAY(1)), 'orderSensitive', false),
  1,
  0
),
(
  18101,
  181,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Name VARCHAR(50), Salary INT, ManagerId INT NULL); INSERT INTO Employee VALUES (1, ''Joe'', 70000, 3), (2, ''Henry'', 80000, 4), (3, ''Sam'', 60000, NULL), (4, ''Max'', 90000, NULL);',
  JSON_OBJECT('columns', JSON_ARRAY('Employee'), 'rows', JSON_ARRAY(JSON_ARRAY('Joe')), 'orderSensitive', false),
  1,
  0
),
(
  18201,
  182,
  'CREATE TABLE Person (Id INT PRIMARY KEY, Email VARCHAR(100)); INSERT INTO Person VALUES (1, ''a@b.com''), (2, ''c@d.com''), (3, ''a@b.com'');',
  JSON_OBJECT('columns', JSON_ARRAY('Email'), 'rows', JSON_ARRAY(JSON_ARRAY('a@b.com')), 'orderSensitive', false),
  1,
  0
),
(
  18301,
  183,
  'CREATE TABLE Customers (Id INT PRIMARY KEY, Name VARCHAR(50)); INSERT INTO Customers VALUES (1, ''Joe''), (2, ''Henry''), (3, ''Sam''), (4, ''Max''); CREATE TABLE Orders (Id INT PRIMARY KEY, CustomerId INT); INSERT INTO Orders VALUES (1, 3), (2, 1);',
  JSON_OBJECT('columns', JSON_ARRAY('Customers'), 'rows', JSON_ARRAY(JSON_ARRAY('Henry'), JSON_ARRAY('Max')), 'orderSensitive', false),
  1,
  0
),
(
  18401,
  184,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Name VARCHAR(50), Salary INT, DepartmentId INT); INSERT INTO Employee VALUES (1, ''Joe'', 70000, 1), (2, ''Henry'', 80000, 2), (3, ''Sam'', 60000, 2), (4, ''Max'', 90000, 1); CREATE TABLE Department (Id INT PRIMARY KEY, Name VARCHAR(50)); INSERT INTO Department VALUES (1, ''IT''), (2, ''Sales'');',
  JSON_OBJECT('columns', JSON_ARRAY('Department', 'Employee', 'Salary'), 'rows', JSON_ARRAY(JSON_ARRAY('IT', 'Max', 90000), JSON_ARRAY('Sales', 'Henry', 80000)), 'orderSensitive', false),
  1,
  0
),
(
  18501,
  185,
  'CREATE TABLE Employee (Id INT PRIMARY KEY, Name VARCHAR(50), Salary INT, DepartmentId INT); INSERT INTO Employee VALUES (1, ''Joe'', 85000, 1), (2, ''Henry'', 80000, 2), (3, ''Sam'', 60000, 2), (4, ''Max'', 90000, 1), (5, ''Janet'', 69000, 1), (6, ''Randy'', 85000, 1), (7, ''Will'', 70000, 1); CREATE TABLE Department (Id INT PRIMARY KEY, Name VARCHAR(50)); INSERT INTO Department VALUES (1, ''IT''), (2, ''Sales'');',
  JSON_OBJECT('columns', JSON_ARRAY('Department', 'Employee', 'Salary'), 'rows', JSON_ARRAY(JSON_ARRAY('IT', 'Max', 90000), JSON_ARRAY('IT', 'Joe', 85000), JSON_ARRAY('IT', 'Randy', 85000), JSON_ARRAY('IT', 'Will', 70000), JSON_ARRAY('Sales', 'Henry', 80000), JSON_ARRAY('Sales', 'Sam', 60000)), 'orderSensitive', false),
  1,
  0
)
ON DUPLICATE KEY UPDATE
  question_id = VALUES(question_id),
  input_sql = VALUES(input_sql),
  expected_output = VALUES(expected_output),
  case_order = VALUES(case_order),
  is_hidden = VALUES(is_hidden);
