package com.scut.mailsystem.mapper.row;

import java.time.LocalDateTime;

public class ThreadListItemRow {

    private Long threadId;
    private String subject;
    private Long lastMailId;
    private String lastContent;
    private String lastSenderUsername;
    private String lastSenderNickname;
    private String lastRecipientUsername;
    private String lastRecipientNickname;
    private LocalDateTime lastSentAt;
    private Integer unreadCount;
    private Integer mailCount;
    private LocalDateTime updatedAt;
    private String priority;
    private Integer spamFlag;
    private String spamLevel;
    private String riskLevel;
    private String riskReason;
    private String analysisStatus;

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getLastMailId() {
        return lastMailId;
    }

    public void setLastMailId(Long lastMailId) {
        this.lastMailId = lastMailId;
    }

    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    public String getLastSenderUsername() {
        return lastSenderUsername;
    }

    public void setLastSenderUsername(String lastSenderUsername) {
        this.lastSenderUsername = lastSenderUsername;
    }

    public String getLastSenderNickname() {
        return lastSenderNickname;
    }

    public void setLastSenderNickname(String lastSenderNickname) {
        this.lastSenderNickname = lastSenderNickname;
    }

    public String getLastRecipientUsername() {
        return lastRecipientUsername;
    }

    public void setLastRecipientUsername(String lastRecipientUsername) {
        this.lastRecipientUsername = lastRecipientUsername;
    }

    public String getLastRecipientNickname() {
        return lastRecipientNickname;
    }

    public void setLastRecipientNickname(String lastRecipientNickname) {
        this.lastRecipientNickname = lastRecipientNickname;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getMailCount() {
        return mailCount;
    }

    public void setMailCount(Integer mailCount) {
        this.mailCount = mailCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }
}
