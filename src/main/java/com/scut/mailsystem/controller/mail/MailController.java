package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/inbox")
    public ApiResponse<PageResult<MailListItemVO>> getInbox(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        return ApiResponse.success(mailService.getInbox(authorizationHeader, page, size));
    }

    @GetMapping("/sent")
    public ApiResponse<PageResult<MailListItemVO>> getSent(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        return ApiResponse.success(mailService.getSent(authorizationHeader, page, size));
    }
}
