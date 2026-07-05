package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;

public class MailListItemVO {

    private Long mailId;
    private Long threadId;
    private Long replyToMailId;
    private String subject;
    private String snippet;
    private MailUserVO sender;
    private MailUserVO recipient;
    private LocalDateTime sentAt;
    private Boolean read;
    private String priority;
    private String priorityLabel;
    private Boolean spam;
    private String spamLevel;
    private String riskLevel;
    private String riskLabel;
    private String riskReason;
    private String analysisStatus;
    private LocalDateTime deletedAt;
    private String spamLevelLabel;

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getReplyToMailId() {
        return replyToMailId;
    }

    public void setReplyToMailId(Long replyToMailId) {
        this.replyToMailId = replyToMailId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public MailUserVO getSender() {
        return sender;
    }

    public void setSender(MailUserVO sender) {
        this.sender = sender;
    }

    public MailUserVO getRecipient() {
        return recipient;
    }

    public void setRecipient(MailUserVO recipient) {
        this.recipient = recipient;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getSpamLevelLabel() {
        return spamLevelLabel;
    }

    public void setSpamLevelLabel(String spamLevelLabel) {
        this.spamLevelLabel = spamLevelLabel;
    }
}
