INSERT INTO users (user_id, email, username, encoded_password, role, is_active, created_by, updated_by)
VALUES
-- Password for all: fhowfiewHdjeiie21432@
(nextval('user_id_seq'), 'admin@test.com', 'admin', '$2a$10$tOee8RQGjMqlTgVONY.HUuD9lEcbsxDxwOmcRypLu3IBn.oiZVz8m', 'ADMIN', true, 0, 0),

(nextval('user_id_seq'), 'qm@test.com', 'qm', '$2a$10$tOee8RQGjMqlTgVONY.HUuD9lEcbsxDxwOmcRypLu3IBn.oiZVz8m', 'QUESTION_MASTER', true, 0, 0),

(nextval('user_id_seq'), 'user@test.com', 'user', '$2a$10$tOee8RQGjMqlTgVONY.HUuD9lEcbsxDxwOmcRypLu3IBn.oiZVz8m', 'USER', true, 0, 0);