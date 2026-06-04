package com.scut.mailsystem.vo.mail;

import java.util.List;

public class MailAnalysisVO {

    private String analysisStatus;
    private String summary;
    private String spamLevel;
    private String spamLevelLabel;
    private String spamReason;
    private String riskLevel;
    private String riskLabel;
    private String priority;
    private String priorityLabel;
    private String priorityReason;
    private String riskReason;
    private List<String> replySuggestions;

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

    public String getSpamLevelLabel() {
        return spamLevelLabel;
    }

    public void setSpamLevelLabel(String spamLevelLabel) {
        this.spamLevelLabel = spamLevelLabel;
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

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
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

    public String getPriorityReason() {
        return priorityReason;
    }

    public void setPriorityReason(String priorityReason) {
        this.priorityReason = priorityReason;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    public List<String> getReplySuggestions() {
        return replySuggestions;
    }

    public void setReplySuggestions(List<String> replySuggestions) {
        this.replySuggestions = replySuggestions;
    }
}
