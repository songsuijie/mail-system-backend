package com.scut.mailsystem.dto.mail;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class SendMailRequest {

    @NotBlank(message = "收件人用户名不能为空")
    private String recipientUsername;

    private String subject;

    private List<Object> content;

    private String attachmentFileId;

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
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

    public String getAttachmentFileId() {
        return attachmentFileId;
    }

    public void setAttachmentFileId(String attachmentFileId) {
        this.attachmentFileId = attachmentFileId;
    }
}
