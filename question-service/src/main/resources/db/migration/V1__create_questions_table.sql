CREATE TABLE IF NOT EXISTS questions (
    question_id   BIGSERIAL       PRIMARY KEY,
    title         VARCHAR(255)    NOT NULL UNIQUE,
    topic         VARCHAR(100)    NOT NULL,
    difficulty    VARCHAR(10)     NOT NULL CHECK (difficulty IN ('easy', 'medium', 'hard')),
    prompt        TEXT            NOT NULL,
    example       TEXT            DEFAULT '[]',
    constraints   TEXT            DEFAULT '[]',
    image_urls    TEXT            DEFAULT '[]',
    created_by    BIGINT,
    created_at    TIMESTAMP       DEFAULT NOW(),
    updated_by    BIGINT,
    updated_at    TIMESTAMP       DEFAULT NOW(),
    is_active     BOOLEAN         DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_questions_topic       ON questions (LOWER(topic));
CREATE INDEX IF NOT EXISTS idx_questions_difficulty  ON questions (LOWER(difficulty));
CREATE INDEX IF NOT EXISTS idx_questions_is_active   ON questions (is_active);
