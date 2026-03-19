INSERT INTO users (user_id, email, username, encoded_password, role, is_active)
VALUES
-- Password for all: fhowfiewHdjeiie21432@
(nextval('user_id_seq'), 'admin@test.com', 'admin', '$2a$10$tOee8RQGjMqlTgVONY.HUuD9lEcbsxDxwOmcRypLu3IBn.oiZVz8m', 'ADMIN', true),

(nextval('user_id_seq'), 'qm@test.com', 'qm', '$2a$10$tOee8RQGjMqlTgVONY.HUuD9lEcbsxDxwOmcRypLu3IBn.oiZVz8m', 'QUESTION_MASTER', true),

(nextval('user_id_seq'), 'user@test.com', 'user', '$2a$10$tOee8RQGjMqlTgVONY.HUuD9lEcbsxDxwOmcRypLu3IBn.oiZVz8m', 'USER', true);