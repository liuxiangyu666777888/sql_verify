USE sql_exam;

UPDATE users
SET password_hash = '$2a$10$XrJUiJwlV9vpp/Lxd9AtFeVMf6tt4rQceMYHI0hudQCcCWBxb3d3.'
WHERE username IN ('admin', 'teacher1', 'student1')
  AND password_hash IN (
    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36y3ThpLS2mA7Szdq1e6i6G',
    'a/Lxd9AtFeVMf6tt4rQceMYHI0hudQCcCWBxb3d3.'
  );
