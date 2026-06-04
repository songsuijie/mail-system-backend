package com.scut.mailsystem.vo.mail;

public class MailReadResponse {

    private Long mailId;
    private Boolean read;

    public MailReadResponse() {
    }

    public MailReadResponse(Long mailId, Boolean read) {
        this.mailId = mailId;
        this.read = read;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
