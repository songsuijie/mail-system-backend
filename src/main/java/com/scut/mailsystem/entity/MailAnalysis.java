package com.scut.mailsystem.entity;

import java.time.LocalDateTime;

public class MailAnalysis {

    private Long id;
    private Long mailId;
    private Long recipientId;
    private String analysisStatus;
    private String priority;
    private Integer priorityScore;
    private String priorityReason;
    private Integer spamFlag;
    private Integer spamScore;
    private String spamLevel;
    private String spamReason;
    private String riskLevel;
    private Integer riskScore;
    private String riskReason;
    private String summary;
    private String replySuggestions;
    private String aiProvider;
    private String modelName;
    private String aiErrorMessage;
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

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Integer priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }

    public Integer getSpamFlag() {
        return spamFlag;
    }

    public void setSpamFlag(Integer spamFlag) {
        this.spamFlag = spamFlag;
    }

    public Integer getSpamScore() {
        return spamScore;
    }

    public void setSpamScore(Integer spamScore) {
        this.spamScore = spamScore;
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

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReplySuggestions() {
        return replySuggestions;
    }

    public void setReplySuggestions(String replySuggestions) {
        this.replySuggestions = replySuggestions;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAiErrorMessage() {
        return aiErrorMessage;
    }

    public void setAiErrorMessage(String aiErrorMessage) {
        this.aiErrorMessage = aiErrorMessage;
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
