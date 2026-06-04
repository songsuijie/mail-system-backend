package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;
import java.util.List;

public class MailDetailVO {

    private Long mailId;
    private String subject;
    private List<Object> content;
    private MailUserVO sender;
    private MailUserVO recipient;
    private LocalDateTime sentAt;
    private String currentUserRole;
    private Boolean read;
    private Boolean deleted;
    private Boolean spam;
    private MailAnalysisVO analysis;
    private MailAttachmentVO attachment;

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

    public List<Object> getContent() {
        return content;
    }

    public void setContent(List<Object> content) {
        this.content = content;
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

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(String currentUserRole) {
        this.currentUserRole = currentUserRole;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getSpam() {
        return spam;
    }

    public void setSpam(Boolean spam) {
        this.spam = spam;
    }

    public MailAnalysisVO getAnalysis() {
        return analysis;
    }

    public void setAnalysis(MailAnalysisVO analysis) {
        this.analysis = analysis;
    }

    public MailAttachmentVO getAttachment() {
        return attachment;
    }

    public void setAttachment(MailAttachmentVO attachment) {
        this.attachment = attachment;
    }
}
