package com.scut.mailsystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.PageResult;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.mail.SendMailRequest;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.entity.MailMessage;
import com.scut.mailsystem.entity.MailRecipient;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.row.MailDetailRow;
import com.scut.mailsystem.mapper.row.MailListItemRow;
import com.scut.mailsystem.mapper.MailAnalysisMapper;
import com.scut.mailsystem.mapper.MailMessageMapper;
import com.scut.mailsystem.mapper.MailRecipientMapper;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.mail.MailService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.mail.MailAnalysisVO;
import com.scut.mailsystem.vo.mail.MailDeleteResponse;
import com.scut.mailsystem.vo.mail.MailDetailVO;
import com.scut.mailsystem.vo.mail.MailListItemVO;
import com.scut.mailsystem.vo.mail.MailReadResponse;
import com.scut.mailsystem.vo.mail.MailUserVO;
import com.scut.mailsystem.vo.mail.SendMailResponse;
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
    private static final int DEFAULT_PRIORITY_SCORE = 50;
    private static final int DEFAULT_SPAM_SCORE = 0;
    private static final int DEFAULT_RISK_SCORE = 0;

    private static final String ANALYSIS_STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String ANALYSIS_STATUS_PENDING = "PENDING";
    private static final String PRIORITY_LOW = "LOW";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String SPAM_LEVEL_NONE = "NONE";
    private static final String RISK_LEVEL_SAFE = "SAFE";
    private static final String RISK_LEVEL_LOW = "LOW";
    private static final String RISK_LEVEL_MEDIUM = "MEDIUM";
    private static final String RISK_LEVEL_HIGH = "HIGH";
    private static final String USER_ROLE_SENDER = "SENDER";
    private static final String USER_ROLE_RECIPIENT = "RECIPIENT";

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
    public PageResult<MailListItemVO> getSent(String authorizationHeader, Integer page, Integer size) {
        SysUser currentUser = getCurrentActiveUser(authorizationHeader);
        PageQuery pageQuery = normalizePageQuery(page, size);

        long total = mailMessageMapper.countSent(currentUser.getId());
        List<MailListItemVO> records = new ArrayList<>();
        if (total > 0) {
            List<MailListItemRow> rows = mailMessageMapper.selectSentPage(
                    currentUser.getId(),
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

    private PageQuery normalizePageQuery(Integer page, Integer size) {
        int normalizedPage = page == null || page < DEFAULT_PAGE ? DEFAULT_PAGE : page;
        int normalizedSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : size;
        if (normalizedSize > MAX_PAGE_SIZE) {
            normalizedSize = MAX_PAGE_SIZE;
        }
        int offset = (normalizedPage - 1) * normalizedSize;
        return new PageQuery(normalizedPage, normalizedSize, offset);
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
        item.setRiskLevel(riskLevel);
        item.setRiskLabel(toRiskLabel(riskLevel));
        item.setRiskReason(row.getRiskReason());
        item.setAnalysisStatus(analysisStatus);
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
        return detail;
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

    private record PageQuery(int page, int size, int offset) {
    }
}
