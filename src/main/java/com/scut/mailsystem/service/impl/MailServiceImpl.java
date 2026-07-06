package com.scut.mailsystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.mail.ReplyEmailRequest;
import com.scut.mailsystem.dto.mail.SendEmailRequest;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.entity.FileResource;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.entity.MailRecipient;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.FileResourceMapper;
import com.scut.mailsystem.mapper.row.MailDetailRow;
import com.scut.mailsystem.mapper.row.MailListItemRow;
import com.scut.mailsystem.mapper.row.ThreadListItemRow;
import com.scut.mailsystem.mapper.row.ThreadMailRow;
import com.scut.mailsystem.mapper.row.ThreadReplyTextRow;
import com.scut.mailsystem.mapper.MailAnalysisMapper;
import com.scut.mailsystem.mapper.MailMessageMapper;
import com.scut.mailsystem.mapper.MailRecipientMapper;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.ai.AiAnalysisService;
import com.scut.mailsystem.service.ai.RuleAnalysisService;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.mail.MailAnalysisVO;
import com.scut.mailsystem.vo.mail.MailAttachmentVO;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailDetailVO;
import com.scut.mailsystem.vo.mail.MailItemVO;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.MailStatisticsVO;
import com.scut.mailsystem.vo.mail.MailUserVO;
import com.scut.mailsystem.vo.mail.RestoreMailResponse;
import com.scut.mailsystem.vo.mail.RetryAnalysisResponse;
import com.scut.mailsystem.vo.mail.SendEmailData;
import com.scut.mailsystem.vo.mail.SendMailResponse;
import com.scut.mailsystem.vo.mail.ThreadDetailVO;
import com.scut.mailsystem.vo.mail.ThreadLastMailVO;
import com.scut.mailsystem.vo.mail.ThreadListItemVO;
import com.scut.mailsystem.vo.mail.ThreadReplyTextData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MailServiceImpl implements MailService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int SNIPPET_MAX_LENGTH = 50;

    private static final int MAIL_STATUS_SENT = 1;
    private static final int NORMAL_RECIPIENT_TYPE = 1;
    private static final int FLAG_NO = 0;
    private static final int FLAG_YES = 1;

    private static final String ANALYSIS_STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String PRIORITY_LOW = "LOW";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String SPAM_LEVEL_NONE = "NONE";
    private static final String SPAM_LEVEL_MEDIUM = "MEDIUM";
    private static final String SPAM_LEVEL_HIGH = "HIGH";
    private static final String RISK_LEVEL_SAFE = "SAFE";
    private static final String RISK_LEVEL_LOW = "LOW";
    private static final String RISK_LEVEL_MEDIUM = "MEDIUM";
    private static final String RISK_LEVEL_HIGH = "HIGH";
    private static final String USER_ROLE_SENDER = "SENDER";
    private static final String USER_ROLE_RECIPIENT = "RECIPIENT";
    private static final String FILE_STATUS_UPLOADED = "UPLOADED";
    private static final String FILE_STATUS_BOUND = "BOUND";

    private final SysUserMapper sysUserMapper;
    private final MailMessageMapper mailMessageMapper;
    private final MailRecipientMapper mailRecipientMapper;
    private final MailAnalysisMapper mailAnalysisMapper;
    private final FileResourceMapper fileResourceMapper;
    private final ObjectMapper objectMapper;
    private final AiAnalysisService aiAnalysisService;
    private final RuleAnalysisService ruleAnalysisService;

    public MailServiceImpl(SysUserMapper sysUserMapper,
                           MailMessageMapper mailMessageMapper,
                           MailRecipientMapper mailRecipientMapper,
                           MailAnalysisMapper mailAnalysisMapper,
                           FileResourceMapper fileResourceMapper,
                           ObjectMapper objectMapper,
                           AiAnalysisService aiAnalysisService,
                           RuleAnalysisService ruleAnalysisService) {
        this.sysUserMapper = sysUserMapper;
        this.mailMessageMapper = mailMessageMapper;
        this.mailRecipientMapper = mailRecipientMapper;
        this.mailAnalysisMapper = mailAnalysisMapper;
        this.fileResourceMapper = fileResourceMapper;
        this.objectMapper = objectMapper;
        this.aiAnalysisService = aiAnalysisService;
        this.ruleAnalysisService = ruleAnalysisService;
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
        FileResource attachment = getUsableAttachment(request.getAttachmentFileId(), sender.getId());

        LocalDateTime now = LocalDateTime.now();
        MailMessage mailMessage = buildMailMessage(
                sender.getId(),
                null,
                null,
                subject,
                content,
                attachment == null ? null : attachment.getFileId(),
                now
        );
        mailMessageMapper.insert(mailMessage);
        mailMessageMapper.updateThreadFields(mailMessage.getId(), mailMessage.getId(), null);
        bindAttachmentIfNecessary(attachment, mailMessage.getId());

        MailAnalysis mailAnalysis = buildMailAnalysis(mailMessage.getId(), recipient.getId(), subject, content, now);
        MailRecipient mailRecipient = buildMailRecipient(mailMessage.getId(), recipient.getId(), mailAnalysis, now);
        mailRecipientMapper.insert(mailRecipient);

        insertMailAnalysisSafely(mailAnalysis, mailMessage.getId(), recipient.getId(), now);

        return new SendMailResponse(
                mailMessage.getId(),
                subject,
                new MailUserVO(sender.getUsername(), sender.getNickname()),
                new MailUserVO(recipient.getUsername(), recipient.getNickname()),
                now,
                mailAnalysis.getAnalysisStatus()
        );
    }

    @Override
    @Transactional
    public SendEmailData sendEmail(String authorizationHeader, SendEmailRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        SysUser sender = getCurrentActiveUser(authorizationHeader);
        SysUser recipient = getActiveRecipient(request.getTo());
        String subject = getSubject(request.getSubject());
        String content = getContentJson(request.getContent());
        FileResource attachment = getUsableAttachment(request.getAttachmentFileId(), sender.getId());

        LocalDateTime now = LocalDateTime.now();
        MailMessage mailMessage = buildMailMessage(
                sender.getId(),
                null,
                null,
                subject,
                content,
                attachment == null ? null : attachment.getFileId(),
                now
        );
        mailMessageMapper.insert(mailMessage);
        mailMessageMapper.updateThreadFields(mailMessage.getId(), mailMessage.getId(), null);
        bindAttachmentIfNecessary(attachment, mailMessage.getId());

        MailAnalysis mailAnalysis = buildMailAnalysis(mailMessage.getId(), recipient.getId(), subject, content, now);
        MailRecipient mailRecipient = buildMailRecipient(mailMessage.getId(), recipient.getId(), mailAnalysis, now);
        mailRecipientMapper.insert(mailRecipient);
        insertMailAnalysisSafely(mailAnalysis, mailMessage.getId(), recipient.getId(), now);

        return new SendEmailData(mailMessage.getId(), mailMessage.getId());
    }

    @Override
    @Transactional
    public SendEmailData replyEmail(String authorizationHeader, ReplyEmailRequest request) {
        if (request == null || request.getMailId() == null || request.getThreadId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        SysUser sender = getCurrentActiveUser(authorizationHeader);
        MailDetailRow original = getExistingMailDetail(request.getMailId());
        boolean currentIsSender = isCurrentSender(sender, original);
        boolean currentIsRecipient = isCurrentRecipient(sender, original);
        if (!currentIsSender && !currentIsRecipient) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Long originalThreadId = original.getThreadId() == null ? original.getMailId() : original.getThreadId();
        if (!request.getThreadId().equals(originalThreadId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Long recipientId = currentIsSender ? original.getRecipientId() : original.getSenderId();
        String subject = StringUtils.hasText(trim(request.getSubject()))
                ? trim(request.getSubject())
                : "Re: " + original.getSubject();
        String content = getContentJson(request.getContent());

        LocalDateTime now = LocalDateTime.now();
        MailMessage mailMessage = buildMailMessage(
                sender.getId(),
                request.getThreadId(),
                original.getMailId(),
                subject,
                content,
                null,
                now
        );
        mailMessageMapper.insert(mailMessage);

        MailAnalysis mailAnalysis = buildMailAnalysis(mailMessage.getId(), recipientId, subject, content, now);
        MailRecipient mailRecipient = buildMailRecipient(mailMessage.getId(), recipientId, mailAnalysis, now);
        mailRecipientMapper.insert(mailRecipient);
        insertMailAnalysisSafely(mailAnalysis, mailMessage.getId(), recipientId, now);

        return new SendEmailData(mailMessage.getId(), request.getThreadId());
    }

    @Override
    public PageResult<MailListItemVO> getInbox(String authorizationHeader, Integer page, Integer size) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        PageQuery pageQuery = normalizePageQuery(page, size);

        long total = mailMessageMapper.countInbox(currentUser.getId());
        List<MailListItemVO> records = new ArrayList<>();
        if (total > 0) {
            List<MailListItemRow> rows = mailMessageMapper.selectInboxPage(
                    currentUser.getId(),
                    pageQuery.offset(),
                    pageQuery.size()
            );
            records = toMailListItemVOList(rows, false);
        }

        return PageResult.of(pageQuery.page(), pageQuery.size(), total, records);
    }

    @Override
    public PageResult<MailListItemVO> getSent(String authorizationHeader,
                                              Integer page,
                                              Integer size,
                                              String keyword,
                                              String recipientUsername,
                                              String startTime,
                                              String endTime) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        PageQuery pageQuery = normalizePageQuery(page, size);
        String normalizedKeyword = trimToNull(keyword);
        String normalizedRecipientUsername = trimToNull(recipientUsername);
        String normalizedStartTime = trimToNull(startTime);
        String normalizedEndTime = trimToNull(endTime);

        long total = mailMessageMapper.countSent(
                currentUser.getId(),
                normalizedKeyword,
                normalizedRecipientUsername,
                normalizedStartTime,
                normalizedEndTime
        );
        List<MailListItemVO> records = new ArrayList<>();
        if (total > 0) {
            List<MailListItemRow> rows = mailMessageMapper.selectSentPage(
                    currentUser.getId(),
                    normalizedKeyword,
                    normalizedRecipientUsername,
                    normalizedStartTime,
                    normalizedEndTime,
                    pageQuery.offset(),
                    pageQuery.size()
            );
            records = toMailListItemVOList(rows, true);
        }

        return PageResult.of(pageQuery.page(), pageQuery.size(), total, records);
    }

    @Override
    @Transactional
    public MailDetailVO getMailDetail(String authorizationHeader, Long mailId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        MailDetailRow row = getExistingMailDetail(mailId);
        boolean recipient = isCurrentRecipient(currentUser, row);
        boolean sender = isCurrentSender(currentUser, row);
        if (!sender && !recipient) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看该邮件");
        }

        if (recipient && !isYes(row.getReadFlag())) {
            LocalDateTime now = LocalDateTime.now();
            mailRecipientMapper.markReadIfUnread(row.getMailId(), currentUser.getId(), now);
            row.setReadFlag(FLAG_YES);
            row.setReadAt(now);
        }

        return toMailDetailVO(row, sender, recipient);
    }

    @Override
    @Transactional
    public MailReadResponse markRead(String authorizationHeader, Long mailId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        MailDetailRow row = getExistingMailDetail(mailId);
        if (!isCurrentRecipient(currentUser, row)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限修改该邮件状态");
        }

        if (!isYes(row.getReadFlag())) {
            mailRecipientMapper.markReadIfUnread(row.getMailId(), currentUser.getId(), LocalDateTime.now());
        }
        return new MailReadResponse(row.getMailId(), true);
    }

    @Override
    @Transactional
    public MailDeleteResponse deleteMail(String authorizationHeader, Long mailId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        MailDetailRow row = getExistingMailDetail(mailId);
        boolean recipient = isCurrentRecipient(currentUser, row);
        boolean sender = isCurrentSender(currentUser, row);
        if (!sender && !recipient) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限删除该邮件");
        }
        if (!recipient) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前版本暂不支持发件人侧删除");
        }

        LocalDateTime deletedAt = row.getDeletedAt();
        if (!isYes(row.getDeletedFlag())) {
            deletedAt = LocalDateTime.now();
            mailRecipientMapper.deleteRecipientMailIfNotDeleted(row.getMailId(), currentUser.getId(), deletedAt);
        }
        return new MailDeleteResponse(row.getMailId(), true, deletedAt);
    }

    @Override
    public PageResult<MailListItemVO> getTrash(String authorizationHeader,
                                               Integer page,
                                               Integer size,
                                               String keyword,
                                               String startTime,
                                               String endTime) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        PageQuery pageQuery = normalizePageQuery(page, size);
        String normalizedKeyword = trimToNull(keyword);
        String normalizedStartTime = trimToNull(startTime);
        String normalizedEndTime = trimToNull(endTime);

        long total = mailMessageMapper.countTrash(
                currentUser.getId(),
                normalizedKeyword,
                normalizedStartTime,
                normalizedEndTime
        );
        List<MailListItemVO> records = new ArrayList<>();
        if (total > 0) {
            records = toMailListItemVOList(mailMessageMapper.selectTrashPage(
                    currentUser.getId(),
                    normalizedKeyword,
                    normalizedStartTime,
                    normalizedEndTime,
                    pageQuery.offset(),
                    pageQuery.size()
            ), false);
        }
        return PageResult.of(pageQuery.page(), pageQuery.size(), total, records);
    }

    @Override
    public PageResult<MailListItemVO> getSpam(String authorizationHeader,
                                              Integer page,
                                              Integer size,
                                              String keyword,
                                              String spamLevel,
                                              String riskLevel,
                                              String startTime,
                                              String endTime) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        PageQuery pageQuery = normalizePageQuery(page, size);
        String normalizedKeyword = trimToNull(keyword);
        String normalizedSpamLevel = trimToNull(spamLevel);
        String normalizedRiskLevel = trimToNull(riskLevel);
        String normalizedStartTime = trimToNull(startTime);
        String normalizedEndTime = trimToNull(endTime);

        long total = mailMessageMapper.countSpam(
                currentUser.getId(),
                normalizedKeyword,
                normalizedSpamLevel,
                normalizedRiskLevel,
                normalizedStartTime,
                normalizedEndTime
        );
        List<MailListItemVO> records = new ArrayList<>();
        if (total > 0) {
            records = toMailListItemVOList(mailMessageMapper.selectSpamPage(
                    currentUser.getId(),
                    normalizedKeyword,
                    normalizedSpamLevel,
                    normalizedRiskLevel,
                    normalizedStartTime,
                    normalizedEndTime,
                    pageQuery.offset(),
                    pageQuery.size()
            ), false);
        }
        return PageResult.of(pageQuery.page(), pageQuery.size(), total, records);
    }

    @Override
    public MailStatisticsVO getStatistics(String authorizationHeader) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        MailStatisticsVO statistics = new MailStatisticsVO();
        statistics.setInboxTotal(toInt(mailMessageMapper.countInbox(currentUser.getId())));
        statistics.setInboxUnread(toInt(mailMessageMapper.countInboxUnread(currentUser.getId())));
        statistics.setSentTotal(toInt(mailMessageMapper.countSent(currentUser.getId(), null, null, null, null)));
        statistics.setTrashTotal(toInt(mailMessageMapper.countTrash(currentUser.getId(), null, null, null)));
        statistics.setSpamTotal(toInt(mailMessageMapper.countSpam(currentUser.getId(), null, null, null, null, null)));
        return statistics;
    }

    @Override
    @Transactional
    public RestoreMailResponse restoreMail(String authorizationHeader, Long mailId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        MailDetailRow row = getExistingMailDetail(mailId);
        if (!isCurrentRecipient(currentUser, row)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限恢复该邮件");
        }
        if (!isYes(row.getDeletedFlag())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "邮件未处于已删除状态");
        }
        mailRecipientMapper.restoreRecipientMailIfDeleted(row.getMailId(), currentUser.getId());
        return new RestoreMailResponse(row.getMailId(), false);
    }

    @Override
    @Transactional
    public RetryAnalysisResponse retryAnalysis(String authorizationHeader, Long mailId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        MailDetailRow row = getExistingMailDetail(mailId);
        boolean recipient = isCurrentRecipient(currentUser, row);
        boolean sender = isCurrentSender(currentUser, row);
        if (!sender && !recipient) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限重新分析该邮件");
        }

        Long analysisRecipientId = recipient ? currentUser.getId() : row.getRecipientId();
        LocalDateTime now = LocalDateTime.now();
        MailAnalysis analysis = buildMailAnalysis(row.getMailId(), analysisRecipientId, row.getSubject(), row.getContent(), now);
        int updated = mailAnalysisMapper.updateByMailAndRecipient(analysis);
        if (updated == 0) {
            mailAnalysisMapper.insert(analysis);
        }
        return new RetryAnalysisResponse(row.getMailId(), analysis.getAnalysisStatus());
    }

    @Override
    public PageResult<ThreadListItemVO> getThreads(String authorizationHeader,
                                                   Integer page,
                                                   Integer size,
                                                   String keyword,
                                                   String readStatus,
                                                   String senderUsername,
                                                   String priority,
                                                   String startTime,
                                                   String endTime) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        PageQuery pageQuery = normalizePageQuery(page, size);
        String normalizedKeyword = trimToNull(keyword);
        String normalizedReadStatus = trimToNull(readStatus);
        String normalizedSenderUsername = trimToNull(senderUsername);
        String normalizedPriority = trimToNull(priority);
        String normalizedStartTime = trimToNull(startTime);
        String normalizedEndTime = trimToNull(endTime);

        long total = mailMessageMapper.countThreads(
                currentUser.getId(),
                normalizedKeyword,
                normalizedReadStatus,
                normalizedSenderUsername,
                normalizedPriority,
                normalizedStartTime,
                normalizedEndTime
        );
        List<ThreadListItemVO> records = new ArrayList<>();
        if (total > 0) {
            List<ThreadListItemRow> rows = mailMessageMapper.selectThreadPage(
                    currentUser.getId(),
                    normalizedKeyword,
                    normalizedReadStatus,
                    normalizedSenderUsername,
                    normalizedPriority,
                    normalizedStartTime,
                    normalizedEndTime,
                    pageQuery.offset(),
                    pageQuery.size()
            );
            records = toThreadListItemVOList(rows);
        }

        return PageResult.of(pageQuery.page(), pageQuery.size(), total, records);
    }

    @Override
    @Transactional
    public ThreadDetailVO getThreadDetail(String authorizationHeader, Long threadId, String cursor, Integer limit) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        if (threadId == null || threadId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        int normalizedLimit = normalizeThreadLimit(limit);
        long total = mailMessageMapper.countThreadMails(threadId, currentUser.getId());
        if (total <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看该邮件线程");
        }

        List<ThreadMailRow> rows = mailMessageMapper.selectThreadMails(threadId, currentUser.getId(), normalizedLimit);
        boolean hasMore = total > rows.size();
        markThreadRowsReadIfNecessary(rows, currentUser.getId());

        ThreadDetailVO detail = new ThreadDetailVO();
        detail.setThreadId(threadId);
        detail.setSubject(rows.isEmpty() ? "" : rows.get(0).getSubject());
        detail.setTotal((int) total);
        detail.setLimit(normalizedLimit);
        detail.setHasMore(hasMore);
        detail.setNextCursor(hasMore && !rows.isEmpty() ? String.valueOf(rows.get(rows.size() - 1).getMailId()) : null);
        detail.setMails(toMailItemVOList(rows));
        detail.setAnalysis(toThreadAnalysisVO(rows));
        return detail;
    }

    @Override
    public ThreadReplyTextData getThreadReplyText(String authorizationHeader, Long threadId) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        if (threadId == null || threadId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (mailMessageMapper.countThreadMailsAll(threadId) <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件线程不存在");
        }
        if (mailMessageMapper.countThreadMails(threadId, currentUser.getId()) <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看该邮件线程");
        }

        ThreadReplyTextRow row = mailMessageMapper.selectLatestThreadReplyText(threadId, currentUser.getId());
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件线程不存在");
        }

        List<String> suggestions = parseReplySuggestions(row.getReplySuggestions());
        ThreadReplyTextData data = new ThreadReplyTextData();
        data.setThreadId(row.getThreadId());
        data.setSourceMailId(row.getSourceMailId());
        data.setReplyText(suggestions.isEmpty() ? "" : suggestions.get(0));
        return data;
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

    private MailDetailRow getExistingMailDetail(Long mailId) {
        if (mailId == null || mailId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        MailDetailRow row = mailMessageMapper.selectDetailByMailId(mailId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return row;
    }

    private boolean isCurrentSender(SysUser currentUser, MailDetailRow row) {
        return currentUser.getId() != null && currentUser.getId().equals(row.getSenderId());
    }

    private boolean isCurrentRecipient(SysUser currentUser, MailDetailRow row) {
        return currentUser.getId() != null && currentUser.getId().equals(row.getRecipientId());
    }

    private FileResource getUsableAttachment(String attachmentFileId, Long senderId) {
        String trimmedAttachmentFileId = trim(attachmentFileId);
        if (!StringUtils.hasText(trimmedAttachmentFileId)) {
            return null;
        }

        FileResource fileResource = fileResourceMapper.selectByFileId(trimmedAttachmentFileId);
        if (fileResource == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        if (!senderId.equals(fileResource.getUploaderId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限使用该文件");
        }
        if (fileResource.getMailId() != null || !FILE_STATUS_UPLOADED.equals(fileResource.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件已被使用");
        }
        return fileResource;
    }

    private void bindAttachmentIfNecessary(FileResource attachment, Long mailId) {
        if (attachment == null) {
            return;
        }
        int updated = fileResourceMapper.bindToMail(attachment.getFileId(), mailId, FILE_STATUS_BOUND);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件已被使用");
        }
    }

    private SysUser getActiveRecipient(SendMailRequest request) {
        return getActiveRecipient(request.getRecipientUsername());
    }

    private SysUser getActiveRecipient(String username) {
        String recipientUsername = trim(username);
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
        return getSubject(request.getSubject());
    }

    private String getSubject(String value) {
        String subject = trim(value);
        if (!StringUtils.hasText(subject)) {
            throw new BusinessException(ErrorCode.MAIL_SUBJECT_EMPTY);
        }
        return subject;
    }

    private String getContentJson(SendMailRequest request) {
        return getContentJson(request.getContent());
    }

    private String getContentJson(List<Object> contentValue) {
        if (contentValue == null || contentValue.isEmpty()) {
            throw new BusinessException(ErrorCode.MAIL_CONTENT_EMPTY);
        }

        try {
            return objectMapper.writeValueAsString(contentValue);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private MailMessage buildMailMessage(Long senderId,
                                         Long threadId,
                                         Long replyToMailId,
                                         String subject,
                                         String content,
                                         String attachmentFileId,
                                         LocalDateTime now) {
        MailMessage mailMessage = new MailMessage();
        mailMessage.setSenderId(senderId);
        mailMessage.setThreadId(threadId);
        mailMessage.setReplyToMailId(replyToMailId);
        mailMessage.setSubject(subject);
        mailMessage.setContent(content);
        mailMessage.setAttachmentFileId(attachmentFileId);
        mailMessage.setSentAt(now);
        mailMessage.setStatus(MAIL_STATUS_SENT);
        mailMessage.setSenderDeleted(FLAG_NO);
        mailMessage.setCreatedAt(now);
        mailMessage.setUpdatedAt(now);
        return mailMessage;
    }

    private MailRecipient buildMailRecipient(Long mailId, Long recipientId, MailAnalysis analysis, LocalDateTime now) {
        MailRecipient mailRecipient = new MailRecipient();
        mailRecipient.setMailId(mailId);
        mailRecipient.setRecipientId(recipientId);
        mailRecipient.setRecipientType(NORMAL_RECIPIENT_TYPE);
        mailRecipient.setReadFlag(FLAG_NO);
        mailRecipient.setReadAt(null);
        mailRecipient.setDeletedFlag(FLAG_NO);
        mailRecipient.setDeletedAt(null);
        mailRecipient.setSpamFlag(analysis.getSpamFlag());
        mailRecipient.setSpamLevel(analysis.getSpamLevel());
        mailRecipient.setRiskLevel(analysis.getRiskLevel());
        mailRecipient.setCreatedAt(now);
        mailRecipient.setUpdatedAt(now);
        return mailRecipient;
    }

    private MailAnalysis buildMailAnalysis(Long mailId,
                                           Long recipientId,
                                           String subject,
                                           String content,
                                           LocalDateTime now) {
        return aiAnalysisService.analyze(mailId, recipientId, subject, content, now);
    }

    private void insertMailAnalysisSafely(MailAnalysis mailAnalysis, Long mailId, Long recipientId, LocalDateTime now) {
        try {
            mailAnalysisMapper.insert(mailAnalysis);
        } catch (RuntimeException firstException) {
            try {
                mailAnalysisMapper.insert(ruleAnalysisService.defaultAnalysis(mailId, recipientId, now));
            } catch (RuntimeException ignored) {
                // Analysis is an enhancement. Sending should still succeed if analysis persistence fails.
            }
        }
    }

    private PageQuery normalizePageQuery(Integer page, Integer size) {
        int normalizedPage = page == null || page < DEFAULT_PAGE ? DEFAULT_PAGE : page;
        int normalizedSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : size;
        if (normalizedSize > MAX_PAGE_SIZE) {
            normalizedSize = MAX_PAGE_SIZE;
        }
        int offset = (normalizedPage - 1) * normalizedSize;
        return new PageQuery(normalizedPage, normalizedSize, offset);
    }

    private int normalizeThreadLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 20;
        }
        return Math.min(limit, MAX_PAGE_SIZE);
    }

    private List<ThreadListItemVO> toThreadListItemVOList(List<ThreadListItemRow> rows) {
        List<ThreadListItemVO> records = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return records;
        }
        for (ThreadListItemRow row : rows) {
            records.add(toThreadListItemVO(row));
        }
        return records;
    }

    private ThreadListItemVO toThreadListItemVO(ThreadListItemRow row) {
        String priority = defaultIfBlank(row.getPriority(), PRIORITY_MEDIUM);
        String riskLevel = defaultIfBlank(row.getRiskLevel(), RISK_LEVEL_SAFE);
        String spamLevel = defaultIfBlank(row.getSpamLevel(), SPAM_LEVEL_NONE);
        String analysisStatus = defaultIfBlank(row.getAnalysisStatus(), ANALYSIS_STATUS_NOT_STARTED);

        ThreadListItemVO item = new ThreadListItemVO();
        item.setThreadId(row.getThreadId());
        item.setSubject(row.getSubject());
        item.setLastSnippet(buildSnippet(row.getLastContent()));
        item.setLastMail(toThreadLastMailVO(row));
        item.setUnreadCount(row.getUnreadCount() == null ? 0 : row.getUnreadCount());
        item.setMailCount(row.getMailCount() == null ? 0 : row.getMailCount());
        item.setUpdatedAt(row.getUpdatedAt());
        item.setPriority(priority);
        item.setPriorityLabel(toPriorityLabel(priority));
        item.setSpam(row.getSpamFlag() != null && row.getSpamFlag() == FLAG_YES);
        item.setSpamLevel(spamLevel);
        item.setRiskLevel(riskLevel);
        item.setRiskLabel(toRiskLabel(riskLevel));
        item.setAnalysisStatus(analysisStatus);
        item.setRiskReason(row.getRiskReason());
        return item;
    }

    private ThreadLastMailVO toThreadLastMailVO(ThreadListItemRow row) {
        ThreadLastMailVO lastMail = new ThreadLastMailVO();
        lastMail.setMailId(row.getLastMailId());
        lastMail.setSender(new MailUserVO(row.getLastSenderUsername(), row.getLastSenderNickname()));
        lastMail.setRecipient(new MailUserVO(row.getLastRecipientUsername(), row.getLastRecipientNickname()));
        lastMail.setSentAt(row.getLastSentAt() == null ? row.getUpdatedAt() : row.getLastSentAt());
        return lastMail;
    }

    private void markThreadRowsReadIfNecessary(List<ThreadMailRow> rows, Long currentUserId) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (ThreadMailRow row : rows) {
            if (currentUserId.equals(row.getRecipientId()) && !isYes(row.getReadFlag())) {
                mailRecipientMapper.markReadIfUnread(row.getMailId(), currentUserId, now);
                row.setReadFlag(FLAG_YES);
            }
        }
    }

    private List<MailItemVO> toMailItemVOList(List<ThreadMailRow> rows) {
        List<MailItemVO> mails = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return mails;
        }
        for (ThreadMailRow row : rows) {
            mails.add(toMailItemVO(row));
        }
        return mails;
    }

    private MailItemVO toMailItemVO(ThreadMailRow row) {
        MailItemVO item = new MailItemVO();
        item.setMailId(row.getMailId());
        item.setThreadId(row.getThreadId());
        item.setReplyToMailId(row.getReplyToMailId());
        item.setSubject(row.getSubject());
        item.setContent(parseRichTextContent(row.getContent()));
        item.setSender(new MailUserVO(row.getSenderUsername(), row.getSenderNickname()));
        item.setRecipient(new MailUserVO(row.getRecipientUsername(), row.getRecipientNickname()));
        item.setSentAt(row.getSentAt());
        item.setAttachment(toMailAttachmentVO(row));
        return item;
    }

    private MailAttachmentVO toMailAttachmentVO(ThreadMailRow row) {
        if (!StringUtils.hasText(row.getAttachmentFileId())) {
            return null;
        }
        MailAttachmentVO attachment = new MailAttachmentVO();
        attachment.setFileId(row.getAttachmentFileId());
        attachment.setOriginalFilename(row.getAttachmentOriginalFilename());
        attachment.setContentType(row.getAttachmentContentType());
        attachment.setFileSize(row.getAttachmentFileSize());
        attachment.setDownloadUrl("/api/files/" + row.getAttachmentFileId() + "/download");
        return attachment;
    }

    private List<MailListItemVO> toMailListItemVOList(List<MailListItemRow> rows, boolean sentList) {
        List<MailListItemVO> records = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return records;
        }

        for (MailListItemRow row : rows) {
            records.add(toMailListItemVO(row, sentList));
        }
        return records;
    }

    private MailListItemVO toMailListItemVO(MailListItemRow row, boolean sentList) {
        MailListItemVO item = new MailListItemVO();
        item.setMailId(row.getMailId());
        item.setThreadId(row.getThreadId());
        item.setReplyToMailId(row.getReplyToMailId());
        item.setSubject(row.getSubject());
        item.setSnippet(buildSnippet(row.getContent()));
        item.setSender(new MailUserVO(row.getSenderUsername(), row.getSenderNickname()));
        item.setRecipient(new MailUserVO(row.getRecipientUsername(), row.getRecipientNickname()));
        item.setSentAt(row.getSentAt());
        item.setRead(sentList ? null : row.getReadFlag() != null && row.getReadFlag() == FLAG_YES);

        String priority = defaultIfBlank(row.getPriority(), PRIORITY_MEDIUM);
        String riskLevel = defaultIfBlank(row.getRiskLevel(), RISK_LEVEL_SAFE);
        String spamLevel = defaultIfBlank(row.getSpamLevel(), SPAM_LEVEL_NONE);
        String analysisStatus = defaultIfBlank(row.getAnalysisStatus(), ANALYSIS_STATUS_NOT_STARTED);

        item.setPriority(priority);
        item.setPriorityLabel(toPriorityLabel(priority));
        item.setSpam(row.getSpamFlag() != null && row.getSpamFlag() == FLAG_YES);
        item.setSpamLevel(spamLevel);
        item.setSpamLevelLabel(toSpamLevelLabel(spamLevel));
        item.setRiskLevel(riskLevel);
        item.setRiskLabel(toRiskLabel(riskLevel));
        item.setRiskReason(row.getRiskReason());
        item.setAnalysisStatus(analysisStatus);
        item.setDeletedAt(row.getDeletedAt());
        return item;
    }

    private MailDetailVO toMailDetailVO(MailDetailRow row, boolean sender, boolean recipient) {
        String priority = defaultIfBlank(row.getPriority(), PRIORITY_MEDIUM);
        String riskLevel = defaultIfBlank(row.getRiskLevel(), RISK_LEVEL_SAFE);
        String spamLevel = defaultIfBlank(row.getSpamLevel(), SPAM_LEVEL_NONE);
        String analysisStatus = defaultIfBlank(row.getAnalysisStatus(), ANALYSIS_STATUS_NOT_STARTED);

        MailDetailVO detail = new MailDetailVO();
        detail.setMailId(row.getMailId());
        detail.setSubject(row.getSubject());
        detail.setContent(parseRichTextContent(row.getContent()));
        detail.setSender(new MailUserVO(row.getSenderUsername(), row.getSenderNickname()));
        detail.setRecipient(new MailUserVO(row.getRecipientUsername(), row.getRecipientNickname()));
        detail.setSentAt(row.getSentAt());
        detail.setCurrentUserRole(recipient ? USER_ROLE_RECIPIENT : USER_ROLE_SENDER);
        detail.setRead(recipient && isYes(row.getReadFlag()));
        detail.setDeleted(recipient ? isYes(row.getDeletedFlag()) : isYes(row.getSenderDeleted()));
        detail.setSpam(isYes(row.getSpamFlag()));
        detail.setAnalysis(toMailAnalysisVO(row, analysisStatus, spamLevel, riskLevel, priority));
        detail.setAttachment(toMailAttachmentVO(row));
        return detail;
    }

    private MailAttachmentVO toMailAttachmentVO(MailDetailRow row) {
        if (!StringUtils.hasText(row.getAttachmentFileId())) {
            return null;
        }

        MailAttachmentVO attachment = new MailAttachmentVO();
        attachment.setFileId(row.getAttachmentFileId());
        attachment.setOriginalFilename(row.getAttachmentOriginalFilename());
        attachment.setContentType(row.getAttachmentContentType());
        attachment.setFileSize(row.getAttachmentFileSize());
        attachment.setDownloadUrl("/api/files/" + row.getAttachmentFileId() + "/download");
        return attachment;
    }

    private MailAnalysisVO toMailAnalysisVO(MailDetailRow row,
                                            String analysisStatus,
                                            String spamLevel,
                                            String riskLevel,
                                            String priority) {
        MailAnalysisVO analysis = new MailAnalysisVO();
        analysis.setAnalysisStatus(analysisStatus);
        analysis.setSummary(defaultIfBlank(row.getSummary(), ""));
        analysis.setSpamLevel(spamLevel);
        analysis.setSpamLevelLabel(toSpamLevelLabel(spamLevel));
        analysis.setSpamReason(defaultIfBlank(row.getSpamReason(), ""));
        analysis.setRiskLevel(riskLevel);
        analysis.setRiskLabel(toRiskLabel(riskLevel));
        analysis.setPriority(priority);
        analysis.setPriorityLabel(toPriorityLabel(priority));
        analysis.setPriorityReason(defaultIfBlank(row.getPriorityReason(), ""));
        analysis.setRiskReason(row.getRiskReason());
        analysis.setReplySuggestions(parseReplySuggestions(row.getReplySuggestions()));
        return analysis;
    }

    private MailAnalysisVO toThreadAnalysisVO(List<ThreadMailRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return defaultMailAnalysisVO();
        }
        ThreadMailRow latest = rows.get(rows.size() - 1);
        String priority = defaultIfBlank(latest.getPriority(), PRIORITY_MEDIUM);
        String riskLevel = defaultIfBlank(latest.getRiskLevel(), RISK_LEVEL_SAFE);
        String spamLevel = defaultIfBlank(latest.getSpamLevel(), SPAM_LEVEL_NONE);
        String analysisStatus = defaultIfBlank(latest.getAnalysisStatus(), ANALYSIS_STATUS_NOT_STARTED);

        MailAnalysisVO analysis = new MailAnalysisVO();
        analysis.setAnalysisStatus(analysisStatus);
        analysis.setSummary(defaultIfBlank(latest.getSummary(), ""));
        analysis.setSpamLevel(spamLevel);
        analysis.setSpamLevelLabel(toSpamLevelLabel(spamLevel));
        analysis.setSpamReason(defaultIfBlank(latest.getSpamReason(), ""));
        analysis.setRiskLevel(riskLevel);
        analysis.setRiskLabel(toRiskLabel(riskLevel));
        analysis.setPriority(priority);
        analysis.setPriorityLabel(toPriorityLabel(priority));
        analysis.setPriorityReason(defaultIfBlank(latest.getPriorityReason(), ""));
        analysis.setRiskReason(latest.getRiskReason());
        analysis.setReplySuggestions(parseReplySuggestions(latest.getReplySuggestions()));
        return analysis;
    }

    private MailAnalysisVO defaultMailAnalysisVO() {
        MailAnalysisVO analysis = new MailAnalysisVO();
        analysis.setAnalysisStatus(ANALYSIS_STATUS_NOT_STARTED);
        analysis.setSummary("");
        analysis.setSpamLevel(SPAM_LEVEL_NONE);
        analysis.setSpamLevelLabel(toSpamLevelLabel(SPAM_LEVEL_NONE));
        analysis.setSpamReason("");
        analysis.setRiskLevel(RISK_LEVEL_SAFE);
        analysis.setRiskLabel(toRiskLabel(RISK_LEVEL_SAFE));
        analysis.setPriority(PRIORITY_MEDIUM);
        analysis.setPriorityLabel(toPriorityLabel(PRIORITY_MEDIUM));
        analysis.setPriorityReason("");
        analysis.setRiskReason(null);
        analysis.setReplySuggestions(Collections.emptyList());
        return analysis;
    }

    private List<Object> parseRichTextContent(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(content, new TypeReference<List<Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    private List<String> parseReplySuggestions(String replySuggestions) {
        if (!StringUtils.hasText(replySuggestions)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(replySuggestions, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException exception) {
            return Collections.emptyList();
        }
    }

    private String buildSnippet(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        String text = extractTextFromJson(content);
        if (!StringUtils.hasText(text)) {
            text = content;
        }

        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= SNIPPET_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, SNIPPET_MAX_LENGTH);
    }

    private String extractTextFromJson(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            StringBuilder builder = new StringBuilder();
            appendTextNodes(root, builder);
            return builder.toString();
        } catch (JsonProcessingException exception) {
            return content;
        }
    }

    private void appendTextNodes(JsonNode node, StringBuilder builder) {
        if (node == null || node.isNull()) {
            return;
        }

        JsonNode textNode = node.get("text");
        if (textNode != null && textNode.isTextual()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(textNode.asText());
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                appendTextNodes(child, builder);
            }
        } else if (node.isObject()) {
            node.fields().forEachRemaining(entry -> appendTextNodes(entry.getValue(), builder));
        }
    }

    private String toPriorityLabel(String priority) {
        if (PRIORITY_LOW.equals(priority)) {
            return "低优先级";
        }
        if (PRIORITY_HIGH.equals(priority)) {
            return "高优先级";
        }
        return "中优先级";
    }

    private String toRiskLabel(String riskLevel) {
        if (RISK_LEVEL_LOW.equals(riskLevel)) {
            return "低风险";
        }
        if (RISK_LEVEL_MEDIUM.equals(riskLevel)) {
            return "中风险";
        }
        if (RISK_LEVEL_HIGH.equals(riskLevel)) {
            return "高风险";
        }
        return "安全";
    }

    private String toSpamLevelLabel(String spamLevel) {
        if (RISK_LEVEL_LOW.equals(spamLevel)) {
            return "低垃圾风险";
        }
        if (RISK_LEVEL_MEDIUM.equals(spamLevel)) {
            return "中垃圾风险";
        }
        if (RISK_LEVEL_HIGH.equals(spamLevel)) {
            return "高垃圾风险";
        }
        return "非垃圾邮件";
    }

    private boolean isYes(Integer flag) {
        return flag != null && flag == FLAG_YES;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    private int toInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private record PageQuery(int page, int size, int offset) {
    }
}
