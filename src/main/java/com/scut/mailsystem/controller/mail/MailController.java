package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.mail.MarkMailReadRequest;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailDetailVO;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.MailStatisticsVO;
import com.scut.mailsystem.vo.mail.RestoreMailResponse;
import com.scut.mailsystem.vo.mail.RetryAnalysisResponse;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/trash")
    public ApiResponse<PageResult<MailListItemVO>> getTrash(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return ApiResponse.success(mailService.getTrash(authorizationHeader, page, size, keyword, startTime, endTime));
    }

    @GetMapping("/spam")
    public ApiResponse<PageResult<MailListItemVO>> getSpam(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "spamLevel", required = false) String spamLevel,
            @RequestParam(value = "riskLevel", required = false) String riskLevel) {
        return ApiResponse.success(mailService.getSpam(authorizationHeader, page, size, spamLevel, riskLevel));
    }

    @GetMapping("/statistics")
    public ApiResponse<MailStatisticsVO> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return ApiResponse.success(mailService.getStatistics(authorizationHeader));
    }

    @GetMapping("/{mailId}")
    public ApiResponse<MailDetailVO> getMailDetail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("mailId") Long mailId) {
        return ApiResponse.success(mailService.getMailDetail(authorizationHeader, mailId));
    }

    @PatchMapping("/{mailId}/read")
    public ApiResponse<MailReadResponse> markRead(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("mailId") Long mailId,
            @RequestBody(required = false) MarkMailReadRequest request) {
        if (request == null || !Boolean.TRUE.equals(request.getRead())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return ApiResponse.success(mailService.markRead(authorizationHeader, mailId));
    }

    @DeleteMapping("/{mailId}")
    public ApiResponse<MailDeleteResponse> deleteMail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("mailId") Long mailId) {
        return ApiResponse.success(mailService.deleteMail(authorizationHeader, mailId));
    }

    @PatchMapping("/{mailId}/restore")
    public ApiResponse<RestoreMailResponse> restoreMail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("mailId") Long mailId) {
        return ApiResponse.success(mailService.restoreMail(authorizationHeader, mailId));
    }

    @PostMapping("/{mailId}/analysis/retry")
    public ApiResponse<RetryAnalysisResponse> retryAnalysis(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("mailId") Long mailId) {
        return ApiResponse.success(mailService.retryAnalysis(authorizationHeader, mailId));
    }
}
