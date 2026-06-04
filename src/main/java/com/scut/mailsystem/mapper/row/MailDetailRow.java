package com.scut.mailsystem.mapper.row;

import java.time.LocalDateTime;

public class MailDetailRow {

    private Long mailId;
    private String subject;
    private String content;
    private Long senderId;
    private String senderUsername;
    private String senderNickname;
    private Long recipientId;
    private String recipientUsername;
    private String recipientNickname;
    private LocalDateTime sentAt;
    private Integer senderDeleted;
    private Integer readFlag;
    private LocalDateTime readAt;
    private Integer deletedFlag;
    private LocalDateTime deletedAt;
    private Integer spamFlag;
    private String analysisStatus;
    private String summary;
    private String spamLevel;
    private String spamReason;
    private String riskLevel;
    private String riskReason;
    private String priority;
    private String priorityReason;
    private String replySuggestions;

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getRecipientNickname() {
        return recipientNickname;
    }

    public void setRecipientNickname(String recipientNickname) {
        this.recipientNickname = recipientNickname;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getSenderDeleted() {
        return senderDeleted;
    }

    public void setSenderDeleted(Integer senderDeleted) {
        this.senderDeleted = senderDeleted;
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

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSpamLevel() {
        return spamLevel;
    }

    public void setSpamLevel(String spamLevel) {
        this.spamLevel = spamLevel;
    }

    public String getSpamReason() {
        return spamReason;
    }

    public void setSpamReason(String spamReason) {
        this.spamReason = spamReason;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }

    public String getReplySuggestions() {
        return replySuggestions;
    }

    public void setReplySuggestions(String replySuggestions) {
        this.replySuggestions = replySuggestions;
    }
}
