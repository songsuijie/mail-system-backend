package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;

public class SendMailResponse {

    private Long mailId;
    private String subject;
    private MailUserVO sender;
    private MailUserVO recipient;
    private LocalDateTime sentAt;
    private String analysisStatus;

    public SendMailResponse() {
    }

    public SendMailResponse(Long mailId, String subject, MailUserVO sender, MailUserVO recipient,
                            LocalDateTime sentAt, String analysisStatus) {
        this.mailId = mailId;
        this.subject = subject;
        this.sender = sender;
        this.recipient = recipient;
        this.sentAt = sentAt;
        this.analysisStatus = analysisStatus;
    }

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

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }
}
