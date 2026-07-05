-- Supplement migration for the final Apifox contract.
-- Run after sql/schema.sql. It is safe to run after
-- sql/attachment-minimal-migration.sql as well.
--
-- Purpose:
-- 1. Add thread fields required by /api/emails/reply and /api/threads.
-- 2. Add attachment storage support required by /api/files and attachmentFileId.
-- 3. Keep old schema files unchanged.

USE mail_system;

-- File upload/download support.
CREATE TABLE IF NOT EXISTS file_resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_id VARCHAR(64) NOT NULL,
    uploader_id BIGINT NOT NULL,
    mail_id BIGINT DEFAULT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_ext VARCHAR(16) NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_resource_file_id (file_id),
    KEY idx_file_resource_uploader (uploader_id),
    KEY idx_file_resource_mail_id (mail_id),
    KEY idx_file_resource_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- mail_message.thread_id
SET @column_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mail_message'
      AND COLUMN_NAME = 'thread_id'
);
SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE mail_message ADD COLUMN thread_id BIGINT DEFAULT NULL AFTER id',
    'SELECT ''column thread_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- mail_message.reply_to_mail_id
SET @column_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mail_message'
      AND COLUMN_NAME = 'reply_to_mail_id'
);
SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE mail_message ADD COLUMN reply_to_mail_id BIGINT DEFAULT NULL AFTER thread_id',
    'SELECT ''column reply_to_mail_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- mail_message.attachment_file_id
SET @column_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mail_message'
      AND COLUMN_NAME = 'attachment_file_id'
);
SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE mail_message ADD COLUMN attachment_file_id VARCHAR(64) DEFAULT NULL AFTER content',
    'SELECT ''column attachment_file_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Existing single mails become one-mail threads.
UPDATE mail_message
SET thread_id = id
WHERE thread_id IS NULL;

-- Indexes for thread list/detail, reply lookup, and attachment lookup.
SET @index_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mail_message'
      AND INDEX_NAME = 'idx_mail_message_thread_sent'
);
SET @sql := IF(
    @index_exists = 0,
    'CREATE INDEX idx_mail_message_thread_sent ON mail_message (thread_id, sent_at)',
    'SELECT ''index idx_mail_message_thread_sent already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mail_message'
      AND INDEX_NAME = 'idx_mail_message_reply_to'
);
SET @sql := IF(
    @index_exists = 0,
    'CREATE INDEX idx_mail_message_reply_to ON mail_message (reply_to_mail_id)',
    'SELECT ''index idx_mail_message_reply_to already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mail_message'
      AND INDEX_NAME = 'idx_mail_message_attachment_file_id'
);
SET @sql := IF(
    @index_exists = 0,
    'CREATE INDEX idx_mail_message_attachment_file_id ON mail_message (attachment_file_id)',
    'SELECT ''index idx_mail_message_attachment_file_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

