package com.scut.mailsystem.vo.mail;

import java.util.List;

public class ThreadDetailVO {

    private Long threadId;
    private String subject;
    private Integer total;
    private Integer limit;
    private String nextCursor;
    private Boolean hasMore;
    private List<MailItemVO> mails;

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<MailItemVO> getMails() {
        return mails;
    }

    public void setMails(List<MailItemVO> mails) {
        this.mails = mails;
    }
}
