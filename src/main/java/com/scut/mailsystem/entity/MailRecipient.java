package com.scut.mailsystem.entity;

import java.time.LocalDateTime;

public class MailRecipient {

    private Long id;
    private Long mailId;
    private Long recipientId;
    private Integer recipientType;
    private Integer readFlag;
    private LocalDateTime readAt;
    private Integer deletedFlag;
    private LocalDateTime deletedAt;
    private Integer spamFlag;
    private String spamLevel;
    private String riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Integer getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(Integer recipientType) {
        this.recipientType = recipientType;
    }

    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Integer getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(Integer deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Integer getSpamFlag() {
        return spamFlag;
    }

    public void setSpamFlag(Integer spamFlag) {
        this.spamFlag = spamFlag;
    }

    public String getSpamLevel() {
        return spamLevel;
    }

    public void setSpamLevel(String spamLevel) {
        this.spamLevel = spamLevel;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
