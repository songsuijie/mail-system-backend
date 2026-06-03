package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mails")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping
    public ApiResponse<SendMailResponse> sendMail(@RequestHeader(value = "Authorization", required = false)
                                                  String authorizationHeader,
                                                  @Valid @RequestBody SendMailRequest request) {
        return ApiResponse.success(mailService.sendMail(authorizationHeader, request));
    }
}
