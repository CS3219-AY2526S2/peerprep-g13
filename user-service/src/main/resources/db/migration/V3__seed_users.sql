-- Seed test users. Password for all: Admin123!
INSERT INTO users (user_id, email, username, encoded_password, role, is_active, created_by, updated_by)
VALUES
(nextval('user_id_seq'), 'admin@test.com',  'admin', '$2b$10$D8/TxLk8pquUTzYmZs/c4e8giWGMftF7w74eBdTpNzGKFT0A6/CQK', 'ADMIN',           true, 0, 0),
(nextval('user_id_seq'), 'qm@test.com',     'qm',    '$2b$10$lYCuuMwKgoKJUDx9zVTm/uMGqMPknJuU1JI8bSb0GShFN4XfoEbw2', 'QUESTION_MASTER', true, 0, 0),
(nextval('user_id_seq'), 'user@test.com',   'user',  '$2b$10$rOzzfo5rBASYHBmNe0bm1Ofd8Y6IhtacqMB5rrHaQv7xDz3j/Cf4K', 'USER',            true, 0, 0);