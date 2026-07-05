package com.scut.mailsystem.mapper.row;

import java.time.LocalDateTime;

public class ThreadMailRow {

    private Long mailId;
    private Long threadId;
    private Long replyToMailId;
    private String subject;
    private String content;
    private Long senderId;
    private String senderUsername;
    private String senderNickname;
    private Long recipientId;
    private String recipientUsername;
    private String recipientNickname;
    private LocalDateTime sentAt;
    private Integer readFlag;
    private String attachmentFileId;
    private String attachmentOriginalFilename;
    private String attachmentContentType;
    private Long attachmentFileSize;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getRecipientNickname() {
        return recipientNickname;
    }

    public void setRecipientNickname(String recipientNickname) {
        this.recipientNickname = recipientNickname;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public String getAttachmentFileId() {
        return attachmentFileId;
    }

    public void setAttachmentFileId(String attachmentFileId) {
        this.attachmentFileId = attachmentFileId;
    }

    public String getAttachmentOriginalFilename() {
        return attachmentOriginalFilename;
    }

    public void setAttachmentOriginalFilename(String attachmentOriginalFilename) {
        this.attachmentOriginalFilename = attachmentOriginalFilename;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }

    public void setAttachmentContentType(String attachmentContentType) {
        this.attachmentContentType = attachmentContentType;
    }

    public Long getAttachmentFileSize() {
        return attachmentFileSize;
    }

    public void setAttachmentFileSize(Long attachmentFileSize) {
        this.attachmentFileSize = attachmentFileSize;
    }
}
