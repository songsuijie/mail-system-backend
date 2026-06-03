package com.scut.mailsystem.vo.auth;

public class LoginUserVO {

    private String username;
    private String nickname;
    private String emailAddress;

    public LoginUserVO() {
    }

    public LoginUserVO(String username, String nickname, String emailAddress) {
        this.username = username;
        this.nickname = nickname;
        this.emailAddress = emailAddress;
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
}
