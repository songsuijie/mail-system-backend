package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.dto.mail.ReplyEmailRequest;
import com.scut.mailsystem.dto.mail.SendEmailRequest;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.SendEmailData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final MailService mailService;

    public EmailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public ApiResponse<SendEmailData> sendEmail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody SendEmailRequest request) {
        return ApiResponse.success(mailService.sendEmail(authorizationHeader, request));
    }

    @PostMapping("/reply")
    public ApiResponse<SendEmailData> replyEmail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ReplyEmailRequest request) {
        return ApiResponse.success(mailService.replyEmail(authorizationHeader, request));
    }
}
