package com.scut.mailsystem.vo.mail;

import java.time.LocalDateTime;

public class MailDeleteResponse {

    private Long mailId;
    private Boolean deleted;
    private LocalDateTime deletedAt;

    public MailDeleteResponse() {
    }

    public MailDeleteResponse(Long mailId, Boolean deleted, LocalDateTime deletedAt) {
        this.mailId = mailId;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
