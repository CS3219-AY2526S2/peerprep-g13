CREATE SEQUENCE IF NOT EXISTS user_id_seq START 1 INCREMENT 1;

CREATE TABLE IF NOT EXISTS users (
    user_id           BIGINT          PRIMARY KEY DEFAULT nextval('user_id_seq'),
    email             VARCHAR(255)    NOT NULL UNIQUE,
    role              VARCHAR(20)     NOT NULL DEFAULT 'USER',
    name              VARCHAR(255),
    username          VARCHAR(255)    NOT NULL,
    encoded_password  VARCHAR(255)    NOT NULL,
    avatar_url        TEXT,
    preferred_language VARCHAR(100),
    preferred_topic   TEXT            DEFAULT '[]',
    created_by        INT             DEFAULT 0,
    created_at        TIMESTAMP       DEFAULT NOW(),
    updated_by        INT             DEFAULT 0,
    updated_at        TIMESTAMP       DEFAULT NOW(),
    is_active         BOOLEAN         DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
