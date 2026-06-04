-- Minimal attachment support migration.
-- Run after sql/schema.sql when attachment upload/download APIs are needed.

USE mail_system;

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

ALTER TABLE mail_message
    ADD COLUMN attachment_file_id VARCHAR(64) DEFAULT NULL AFTER content;

CREATE INDEX idx_mail_message_attachment_file_id
    ON mail_message (attachment_file_id);
