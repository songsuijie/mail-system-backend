package com.scut.mailsystem.mapper.row;

public class ThreadReplyTextRow {

    private Long threadId;
    private Long sourceMailId;
    private String replySuggestions;

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

    public String getReplySuggestions() {
        return replySuggestions;
    }

    public void setReplySuggestions(String replySuggestions) {
        this.replySuggestions = replySuggestions;
    }
}
