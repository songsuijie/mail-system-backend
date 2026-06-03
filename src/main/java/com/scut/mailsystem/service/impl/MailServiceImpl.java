package com.scut.mailsystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.entity.MailRecipient;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.MailAnalysisMapper;
import com.scut.mailsystem.mapper.MailMessageMapper;
import com.scut.mailsystem.mapper.MailRecipientMapper;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.mail.MailUserVO;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class MailServiceImpl implements MailService {

    private static final int MAIL_STATUS_SENT = 1;
    private static final int NORMAL_RECIPIENT_TYPE = 1;
    private static final int FLAG_NO = 0;
    private static final int DEFAULT_PRIORITY_SCORE = 50;
    private static final int DEFAULT_SPAM_SCORE = 0;
    private static final int DEFAULT_RISK_SCORE = 0;

    private static final String ANALYSIS_STATUS_PENDING = "PENDING";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String SPAM_LEVEL_NONE = "NONE";
    private static final String RISK_LEVEL_SAFE = "SAFE";

    private final SysUserMapper sysUserMapper;
    private final MailMessageMapper mailMessageMapper;
    private final MailRecipientMapper mailRecipientMapper;
    private final MailAnalysisMapper mailAnalysisMapper;
    private final ObjectMapper objectMapper;

    public MailServiceImpl(SysUserMapper sysUserMapper,
                           MailMessageMapper mailMessageMapper,
                           MailRecipientMapper mailRecipientMapper,
                           MailAnalysisMapper mailAnalysisMapper,
                           ObjectMapper objectMapper) {
        this.sysUserMapper = sysUserMapper;
        this.mailMessageMapper = mailMessageMapper;
        this.mailRecipientMapper = mailRecipientMapper;
        this.mailAnalysisMapper = mailAnalysisMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public SendMailResponse sendMail(String authorizationHeader, SendMailRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        SysUser sender = getCurrentActiveUser(authorizationHeader);
        SysUser recipient = getActiveRecipient(request);
        String subject = getSubject(request);
        String content = getContentJson(request);

        LocalDateTime now = LocalDateTime.now();
        MailMessage mailMessage = buildMailMessage(sender.getId(), subject, content, now);
        mailMessageMapper.insert(mailMessage);

        MailRecipient mailRecipient = buildMailRecipient(mailMessage.getId(), recipient.getId(), now);
        mailRecipientMapper.insert(mailRecipient);

        MailAnalysis mailAnalysis = buildMailAnalysis(mailMessage.getId(), recipient.getId(), now);
        mailAnalysisMapper.insert(mailAnalysis);

        return new SendMailResponse(
                mailMessage.getId(),
                subject,
                new MailUserVO(sender.getUsername(), sender.getNickname()),
                new MailUserVO(recipient.getUsername(), recipient.getNickname()),
                now,
                ANALYSIS_STATUS_PENDING
        );
    }

    private SysUser getCurrentActiveUser(String authorizationHeader) {
        String token = AuthHeaderUtils.extractToken(authorizationHeader);
        LoginUser loginUser = TokenUtils.parseToken(token);
        SysUser sender = sysUserMapper.selectActiveById(loginUser.getUserId());
        if (sender == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return sender;
    }

    private SysUser getActiveRecipient(SendMailRequest request) {
        String recipientUsername = trim(request.getRecipientUsername());
        if (!StringUtils.hasText(recipientUsername)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        SysUser recipient = sysUserMapper.selectActiveByUsername(recipientUsername);
        if (recipient == null) {
            throw new BusinessException(ErrorCode.RECIPIENT_NOT_FOUND);
        }
        return recipient;
    }

    private String getSubject(SendMailRequest request) {
        String subject = trim(request.getSubject());
        if (!StringUtils.hasText(subject)) {
            throw new BusinessException(ErrorCode.MAIL_SUBJECT_EMPTY);
        }
        return subject;
    }

    private String getContentJson(SendMailRequest request) {
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new BusinessException(ErrorCode.MAIL_CONTENT_EMPTY);
        }

        try {
            return objectMapper.writeValueAsString(request.getContent());
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private MailMessage buildMailMessage(Long senderId, String subject, String content, LocalDateTime now) {
        MailMessage mailMessage = new MailMessage();
        mailMessage.setSenderId(senderId);
        mailMessage.setSubject(subject);
        mailMessage.setContent(content);
        mailMessage.setSentAt(now);
        mailMessage.setStatus(MAIL_STATUS_SENT);
        mailMessage.setSenderDeleted(FLAG_NO);
        mailMessage.setCreatedAt(now);
        mailMessage.setUpdatedAt(now);
        return mailMessage;
    }

    private MailRecipient buildMailRecipient(Long mailId, Long recipientId, LocalDateTime now) {
        MailRecipient mailRecipient = new MailRecipient();
        mailRecipient.setMailId(mailId);
        mailRecipient.setRecipientId(recipientId);
        mailRecipient.setRecipientType(NORMAL_RECIPIENT_TYPE);
        mailRecipient.setReadFlag(FLAG_NO);
        mailRecipient.setReadAt(null);
        mailRecipient.setDeletedFlag(FLAG_NO);
        mailRecipient.setDeletedAt(null);
        mailRecipient.setSpamFlag(FLAG_NO);
        mailRecipient.setSpamLevel(SPAM_LEVEL_NONE);
        mailRecipient.setRiskLevel(RISK_LEVEL_SAFE);
        mailRecipient.setCreatedAt(now);
        mailRecipient.setUpdatedAt(now);
        return mailRecipient;
    }

    private MailAnalysis buildMailAnalysis(Long mailId, Long recipientId, LocalDateTime now) {
        MailAnalysis mailAnalysis = new MailAnalysis();
        mailAnalysis.setMailId(mailId);
        mailAnalysis.setRecipientId(recipientId);
        mailAnalysis.setAnalysisStatus(ANALYSIS_STATUS_PENDING);
        mailAnalysis.setPriority(PRIORITY_MEDIUM);
        mailAnalysis.setPriorityScore(DEFAULT_PRIORITY_SCORE);
        mailAnalysis.setPriorityReason(null);
        mailAnalysis.setSpamFlag(FLAG_NO);
        mailAnalysis.setSpamScore(DEFAULT_SPAM_SCORE);
        mailAnalysis.setSpamLevel(SPAM_LEVEL_NONE);
        mailAnalysis.setSpamReason(null);
        mailAnalysis.setRiskLevel(RISK_LEVEL_SAFE);
        mailAnalysis.setRiskScore(DEFAULT_RISK_SCORE);
        mailAnalysis.setRiskReason(null);
        mailAnalysis.setSummary(null);
        mailAnalysis.setReplySuggestions(null);
        mailAnalysis.setAiProvider(null);
        mailAnalysis.setModelName(null);
        mailAnalysis.setAiErrorMessage(null);
        mailAnalysis.setCreatedAt(now);
        mailAnalysis.setUpdatedAt(now);
        return mailAnalysis;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
