package com.scut.mailsystem.controller.mail;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.vo.mail.ThreadDetailVO;
import com.scut.mailsystem.vo.mail.ThreadListItemVO;
import com.scut.mailsystem.vo.mail.ThreadReplyTextData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    private final MailService mailService;

    public ThreadController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping
    public ApiResponse<PageResult<ThreadListItemVO>> getThreads(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "readStatus", required = false) String readStatus,
            @RequestParam(value = "senderUsername", required = false) String senderUsername,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return ApiResponse.success(mailService.getThreads(
                authorizationHeader,
                page,
                size,
                keyword,
                readStatus,
                senderUsername,
                priority,
                startTime,
                endTime
        ));
    }

    @GetMapping("/{threadId}")
    public ApiResponse<ThreadDetailVO> getThreadDetail(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("threadId") Long threadId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(mailService.getThreadDetail(authorizationHeader, threadId, cursor, limit));
    }

    @GetMapping("/{threadId}/reply-text")
    public ApiResponse<ThreadReplyTextData> getThreadReplyText(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("threadId") Long threadId) {
        return ApiResponse.success(mailService.getThreadReplyText(authorizationHeader, threadId));
    }
}
