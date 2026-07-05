package com.scut.mailsystem.vo.mail;

public class SendEmailData {

    private Long mailId;
    private Long threadId;

    public SendEmailData() {
    }

    public SendEmailData(Long mailId, Long threadId) {
        this.mailId = mailId;
        this.threadId = threadId;
    }

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
}
