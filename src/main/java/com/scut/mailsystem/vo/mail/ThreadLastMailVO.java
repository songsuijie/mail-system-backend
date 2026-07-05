package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;

public class ThreadLastMailVO {

    private Long mailId;
    private MailUserVO sender;
    private MailUserVO recipient;
    private LocalDateTime sentAt;

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
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
}
