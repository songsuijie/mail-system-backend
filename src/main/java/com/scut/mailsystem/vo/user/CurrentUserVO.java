package com.scut.mailsystem.vo.user;

public class CurrentUserVO {

    private String username;
    private String nickname;
    private String emailAddress;
    private String avatarText;

    public CurrentUserVO() {
    }

    public CurrentUserVO(String username, String nickname, String emailAddress, String avatarText) {
        this.username = username;
        this.nickname = nickname;
        this.emailAddress = emailAddress;
        this.avatarText = avatarText;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAvatarText() {
        return avatarText;
    }

    public void setAvatarText(String avatarText) {
        this.avatarText = avatarText;
    }
}
