package com.scut.mailsystem.vo.mail;

public class ThreadReplyTextData {

    private Long threadId;
    private Long sourceMailId;
    private String replyText;

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getSourceMailId() {
        return sourceMailId;
    }

    public void setSourceMailId(Long sourceMailId) {
        this.sourceMailId = sourceMailId;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }
}
