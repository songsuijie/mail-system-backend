package com.scut.mailsystem.vo.mail;

public class RestoreMailResponse {

    private Long mailId;
    private Boolean deleted;

    public RestoreMailResponse() {
    }

    public RestoreMailResponse(Long mailId, Boolean deleted) {
        this.mailId = mailId;
        this.deleted = deleted;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
