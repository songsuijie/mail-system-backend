package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;
import java.util.List;

public class MailItemVO {

    private Long mailId;
    private Long threadId;
    private Long replyToMailId;
    private String subject;
    private List<Object> content;
    private MailUserVO sender;
    private MailUserVO recipient;
    private LocalDateTime sentAt;
    private MailAttachmentVO attachment;

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

    public MailAttachmentVO getAttachment() {
        return attachment;
    }

    public void setAttachment(MailAttachmentVO attachment) {
        this.attachment = attachment;
    }
}
