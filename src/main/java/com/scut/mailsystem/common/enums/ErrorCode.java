package com.scut.mailsystem.common.enums;

public enum ErrorCode {

    SUCCESS(0, "success"),
    PARAM_ERROR(40000, "请求参数错误"),
    INVALID_USERNAME_OR_PASSWORD(40001, "用户名或密码错误"),
    UNAUTHORIZED(40002, "未登录或 token 无效"),
    FORBIDDEN(40003, "无权限访问该资源"),
    NOT_FOUND(40004, "资源不存在"),
    USERNAME_EXISTS(40005, "用户名已存在"),
    RECIPIENT_NOT_FOUND(40006, "收件人不存在"),
    MAIL_SUBJECT_EMPTY(40007, "邮件主题不能为空"),
    MAIL_CONTENT_EMPTY(40008, "邮件正文不能为空"),
    SYSTEM_ERROR(50000, "系统内部错误"),
    AI_ANALYSIS_FAILED(50001, "AI 分析失败，但不影响邮件主流程");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
