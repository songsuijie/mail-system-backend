package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;

public class ThreadListItemVO {

    private Long threadId;
    private String subject;
    private String lastSnippet;
    private ThreadLastMailVO lastMail;
    private Integer unreadCount;
    private Integer mailCount;
    private LocalDateTime updatedAt;
    private String priority;
    private String priorityLabel;
    private Boolean spam;
    private String spamLevel;
    private String riskLevel;
    private String riskLabel;
    private String analysisStatus;
    private String riskReason;

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

    public String getLastSnippet() {
        return lastSnippet;
    }

    public void setLastSnippet(String lastSnippet) {
        this.lastSnippet = lastSnippet;
    }

    public ThreadLastMailVO getLastMail() {
        return lastMail;
    }

    public void setLastMail(ThreadLastMailVO lastMail) {
        this.lastMail = lastMail;
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

    public String getPriorityLabel() {
        return priorityLabel;
    }

    public void setPriorityLabel(String priorityLabel) {
        this.priorityLabel = priorityLabel;
    }

    public Boolean getSpam() {
        return spam;
    }

    public void setSpam(Boolean spam) {
        this.spam = spam;
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

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }
}
