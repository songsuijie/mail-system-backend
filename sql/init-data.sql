-- Mail System Backend initial data.
-- Run after sql/schema.sql.
-- Default password for all users is 123456.
-- password_hash uses SHA2('123456', 256).

USE mail_system;

INSERT INTO sys_user (
    username,
    password_hash,
    nickname,
    email_address,
    status,
    deleted
) VALUES
    ('admin', SHA2('123456', 256), '管理员', 'admin@mail.com', 1, 0),
    ('alice', SHA2('123456', 256), 'Alice', 'alice@mail.com', 1, 0),
    ('bob', SHA2('123456', 256), 'Bob', 'bob@mail.com', 1, 0)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    nickname = VALUES(nickname),
    email_address = VALUES(email_address),
    status = VALUES(status),
    deleted = VALUES(deleted),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_settings (
    user_id,
    ai_enabled,
    auto_reply_enabled,
    priority_sort_enabled,
    timeout_ms,
    max_tokens,
    temperature
)
SELECT
    id,
    0,
    0,
    1,
    10000,
    800,
    0.20
FROM sys_user
WHERE username IN ('admin', 'alice', 'bob')
ON DUPLICATE KEY UPDATE
    ai_enabled = VALUES(ai_enabled),
    auto_reply_enabled = VALUES(auto_reply_enabled),
    priority_sort_enabled = VALUES(priority_sort_enabled),
    timeout_ms = VALUES(timeout_ms),
    max_tokens = VALUES(max_tokens),
    temperature = VALUES(temperature),
    updated_at = CURRENT_TIMESTAMP;

