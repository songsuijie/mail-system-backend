package com.scut.mailsystem.vo.mail;

public class RetryAnalysisResponse {

    private Long mailId;
    private String analysisStatus;

    public RetryAnalysisResponse() {
    }

    public RetryAnalysisResponse(Long mailId, String analysisStatus) {
        this.mailId = mailId;
        this.analysisStatus = analysisStatus;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }
}
