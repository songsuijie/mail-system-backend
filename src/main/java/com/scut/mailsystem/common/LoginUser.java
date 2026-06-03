package com.scut.mailsystem.common;

public class LoginUser {

    private Long userId;
    private String username;
    private Long expireAt;

    public LoginUser() {
    }

    public LoginUser(Long userId, String username, Long expireAt) {
        this.userId = userId;
        this.username = username;
        this.expireAt = expireAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }
}
