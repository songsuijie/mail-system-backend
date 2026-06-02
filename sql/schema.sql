-- Mail System Backend schema.
-- Database: MySQL 8.x or compatible versions.
-- This script is intended for local development reset.

CREATE DATABASE IF NOT EXISTS mail_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE mail_system;

DROP TABLE IF EXISTS mail_analysis;
DROP TABLE IF EXISTS mail_recipient;
DROP TABLE IF EXISTS mail_message;
DROP TABLE IF EXISTS user_settings;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    email_address VARCHAR(128) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ai_enabled TINYINT NOT NULL DEFAULT 0,
    auto_reply_enabled TINYINT NOT NULL DEFAULT 0,
    priority_sort_enabled TINYINT NOT NULL DEFAULT 1,
    provider VARCHAR(32) DEFAULT NULL,
    base_url VARCHAR(255) DEFAULT NULL,
    model_name VARCHAR(128) DEFAULT NULL,
    api_key_encrypted VARCHAR(1024) DEFAULT NULL,
    api_key_mask VARCHAR(64) DEFAULT NULL,
    timeout_ms INT NOT NULL DEFAULT 10000,
    max_tokens INT NOT NULL DEFAULT 800,
    temperature DECIMAL(3,2) NOT NULL DEFAULT 0.20,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_settings_user_id (user_id),
    KEY idx_user_settings_provider (provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mail_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    subject VARCHAR(200) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 1,
    sender_deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_mail_message_sender_sent (sender_id, sender_deleted, sent_at),
    KEY idx_mail_message_sent_at (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mail_recipient (
    id BIGINT NOT NULL AUTO_INCREMENT,
    mail_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    recipient_type TINYINT NOT NULL DEFAULT 1,
    read_flag TINYINT NOT NULL DEFAULT 0,
    read_at DATETIME DEFAULT NULL,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    deleted_at DATETIME DEFAULT NULL,
    spam_flag TINYINT NOT NULL DEFAULT 0,
    spam_level VARCHAR(16) NOT NULL DEFAULT 'NONE',
    risk_level VARCHAR(16) NOT NULL DEFAULT 'SAFE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mail_recipient_unique (mail_id, recipient_id, recipient_type),
    KEY idx_mail_recipient_inbox (recipient_id, deleted_flag, spam_flag, read_flag),
    KEY idx_mail_recipient_trash (recipient_id, deleted_flag, deleted_at),
    KEY idx_mail_recipient_spam (recipient_id, deleted_flag, spam_flag, risk_level),
    KEY idx_mail_recipient_mail_id (mail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mail_analysis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    mail_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    analysis_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    priority VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    priority_score INT NOT NULL DEFAULT 50,
    priority_reason VARCHAR(500) DEFAULT NULL,
    spam_flag TINYINT NOT NULL DEFAULT 0,
    spam_score INT NOT NULL DEFAULT 0,
    spam_level VARCHAR(16) NOT NULL DEFAULT 'NONE',
    spam_reason VARCHAR(500) DEFAULT NULL,
    risk_level VARCHAR(16) NOT NULL DEFAULT 'SAFE',
    risk_score INT NOT NULL DEFAULT 0,
    risk_reason VARCHAR(500) DEFAULT NULL,
    summary TEXT NULL,
    reply_suggestions JSON NULL,
    ai_provider VARCHAR(64) DEFAULT NULL,
    model_name VARCHAR(128) DEFAULT NULL,
    ai_error_message VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mail_analysis_mail_recipient (mail_id, recipient_id),
    KEY idx_mail_analysis_status (analysis_status),
    KEY idx_mail_analysis_priority (priority, priority_score),
    KEY idx_mail_analysis_spam (spam_flag, spam_level),
    KEY idx_mail_analysis_risk (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

