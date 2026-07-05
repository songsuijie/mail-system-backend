package com.scut.mailsystem.vo.mail;

public class MailStatisticsVO {

    private Integer inboxTotal;
    private Integer inboxUnread;
    private Integer sentTotal;
    private Integer trashTotal;
    private Integer spamTotal;

    public Integer getInboxTotal() {
        return inboxTotal;
    }

    public void setInboxTotal(Integer inboxTotal) {
        this.inboxTotal = inboxTotal;
    }

    public Integer getInboxUnread() {
        return inboxUnread;
    }

    public void setInboxUnread(Integer inboxUnread) {
        this.inboxUnread = inboxUnread;
    }

    public Integer getSentTotal() {
        return sentTotal;
    }

    public void setSentTotal(Integer sentTotal) {
        this.sentTotal = sentTotal;
    }

    public Integer getTrashTotal() {
        return trashTotal;
    }

    public void setTrashTotal(Integer trashTotal) {
        this.trashTotal = trashTotal;
    }

    public Integer getSpamTotal() {
        return spamTotal;
    }

    public void setSpamTotal(Integer spamTotal) {
        this.spamTotal = spamTotal;
    }
}
