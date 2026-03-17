CREATE TABLE IF NOT EXISTS history (
    history_id    BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    question_id   BIGINT       NOT NULL,
    finished_date TIMESTAMP    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_history_user_id ON history (user_id);