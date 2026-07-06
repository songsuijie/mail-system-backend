package com.scut.mailsystem.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.service.ai.RuleAnalysisService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RuleAnalysisServiceImpl implements RuleAnalysisService {

    private static final int FLAG_NO = 0;
    private static final int FLAG_YES = 1;
    private static final int SUMMARY_MAX_LENGTH = 80;

    private static final String ANALYSIS_STATUS_SUCCESS = "SUCCESS";
    private static final String PRIORITY_LOW = "LOW";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String SPAM_LEVEL_NONE = "NONE";
    private static final String SPAM_LEVEL_LOW = "LOW";
    private static final String SPAM_LEVEL_MEDIUM = "MEDIUM";
    private static final String SPAM_LEVEL_HIGH = "HIGH";
    private static final String RISK_LEVEL_SAFE = "SAFE";
    private static final String RISK_LEVEL_MEDIUM = "MEDIUM";
    private static final String RISK_LEVEL_HIGH = "HIGH";
    private static final String DEFAULT_SUMMARY = "该邮件暂无可提取摘要。";
    private static final List<String> DEFAULT_REPLY_SUGGESTIONS = List.of(
            "收到，我会尽快处理。",
            "好的，如有问题我会及时反馈。"
    );
    private static final List<String> CAUTIOUS_REPLY_SUGGESTIONS = List.of(
            "请先核实邮件来源后再处理。",
            "我会确认信息真实性后再回复。"
    );
    private static final List<String> PRIORITY_KEYWORDS = List.of(
            "紧急", "重要", "截止", "今天", "明天", "尽快", "提交", "马上", "立即", "限时", "最后期限",
            "deadline", "urgent", "asap", "action required", "final notice", "due today"
    );
    private static final List<String> SPAM_KEYWORDS = List.of(
            "中奖", "返现", "返利", "优惠券", "免费领取", "点击领取", "广告", "推广", "贷款", "博彩", "抽奖",
            "礼品卡", "gift card", "google play", "apple card", "amazon card", "免费", "限时优惠",
            "unsubscribe", "promotion", "winner", "lottery", "prize", "cash back", "casino", "loan"
    );
    private static final List<String> RISK_KEYWORDS = List.of(
            "登录验证", "账号异常", "账号冻结", "账户锁定", "安全验证", "密码", "修改密码", "银行卡", "银行账号",
            "信用卡", "身份证", "社保", "转账", "汇款", "验证码", "pin", "cvv", "个人信息", "财务信息",
            "钓鱼", "立即登录", "点击链接", "下载附件", "支付信息", "付款失败", "礼品卡", "gift card",
            "verify account", "password reset", "sign in", "login", "update payment", "wire transfer",
            "http://", "bit.ly", "tinyurl", "t.co", "goo.gl", "is.gd", "ow.ly", "rebrand.ly",
            "s.id", "url.cn", "t.cn", "dwz.cn", "shorturl.at"
    );

    private final ObjectMapper objectMapper;

    public RuleAnalysisServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public MailAnalysis analyze(Long mailId, Long recipientId, String subject, String content, LocalDateTime now) {
        try {
            return buildRuleBasedMailAnalysis(mailId, recipientId, subject, content, now);
        } catch (RuntimeException exception) {
            return defaultAnalysis(mailId, recipientId, now);
        }
    }

    @Override
    public MailAnalysis defaultAnalysis(Long mailId, Long recipientId, LocalDateTime now) {
        MailAnalysis mailAnalysis = new MailAnalysis();
        mailAnalysis.setMailId(mailId);
        mailAnalysis.setRecipientId(recipientId);
        mailAnalysis.setAnalysisStatus(ANALYSIS_STATUS_SUCCESS);
        mailAnalysis.setPriority(PRIORITY_MEDIUM);
        mailAnalysis.setPriorityScore(50);
        mailAnalysis.setPriorityReason("规则分析不可用，使用默认优先级。");
        mailAnalysis.setSpamFlag(FLAG_NO);
        mailAnalysis.setSpamScore(0);
        mailAnalysis.setSpamLevel(SPAM_LEVEL_NONE);
        mailAnalysis.setSpamReason("规则分析不可用，默认未发现垃圾邮件特征。");
        mailAnalysis.setRiskLevel(RISK_LEVEL_SAFE);
        mailAnalysis.setRiskScore(0);
        mailAnalysis.setRiskReason(null);
        mailAnalysis.setSummary(DEFAULT_SUMMARY);
        mailAnalysis.setReplySuggestions(toJsonSuggestions(DEFAULT_REPLY_SUGGESTIONS));
        mailAnalysis.setAiProvider(null);
        mailAnalysis.setModelName(null);
        mailAnalysis.setAiErrorMessage(null);
        mailAnalysis.setCreatedAt(now);
        mailAnalysis.setUpdatedAt(now);
        return mailAnalysis;
    }

    private MailAnalysis buildRuleBasedMailAnalysis(Long mailId,
                                                    Long recipientId,
                                                    String subject,
                                                    String content,
                                                    LocalDateTime now) {
        String plainText = normalizeText(extractTextFromJson(content));
        String rawText = normalizeText(defaultIfBlank(subject, "") + " " + plainText + " " + defaultIfBlank(content, ""));
        List<String> priorityMatches = findKeywordHits(rawText, PRIORITY_KEYWORDS);
        List<String> spamMatches = findKeywordHits(rawText, SPAM_KEYWORDS);
        List<String> riskMatches = findKeywordHits(rawText, RISK_KEYWORDS);

        MailAnalysis mailAnalysis = new MailAnalysis();
        mailAnalysis.setMailId(mailId);
        mailAnalysis.setRecipientId(recipientId);
        mailAnalysis.setAnalysisStatus(ANALYSIS_STATUS_SUCCESS);
        applyPriorityAnalysis(mailAnalysis, priorityMatches, plainText);
        applySpamAnalysis(mailAnalysis, spamMatches);
        applyRiskAnalysis(mailAnalysis, riskMatches);
        mailAnalysis.setSummary(buildSummary(plainText));
        mailAnalysis.setReplySuggestions(toJsonSuggestions(
                isCautiousReplyNeeded(mailAnalysis) ? CAUTIOUS_REPLY_SUGGESTIONS : DEFAULT_REPLY_SUGGESTIONS
        ));
        mailAnalysis.setAiProvider(null);
        mailAnalysis.setModelName(null);
        mailAnalysis.setAiErrorMessage(null);
        mailAnalysis.setCreatedAt(now);
        mailAnalysis.setUpdatedAt(now);
        return mailAnalysis;
    }

    private void applyPriorityAnalysis(MailAnalysis mailAnalysis, List<String> priorityMatches, String plainText) {
        if (!priorityMatches.isEmpty()) {
            mailAnalysis.setPriority(PRIORITY_HIGH);
            mailAnalysis.setPriorityScore(Math.min(100, 80 + priorityMatches.size() * 5));
            mailAnalysis.setPriorityReason("命中高优先级信号：" + formatMatches(priorityMatches) + "。");
            return;
        }
        if (!StringUtils.hasText(plainText) || plainText.length() < 20) {
            mailAnalysis.setPriority(PRIORITY_LOW);
            mailAnalysis.setPriorityScore(25);
            mailAnalysis.setPriorityReason("正文较短，未发现明确任务或截止信息。");
            return;
        }
        mailAnalysis.setPriority(PRIORITY_MEDIUM);
        mailAnalysis.setPriorityScore(50);
        mailAnalysis.setPriorityReason("未发现高优先级关键词，按普通邮件处理。");
    }

    private void applySpamAnalysis(MailAnalysis mailAnalysis, List<String> spamMatches) {
        int spamHits = spamMatches.size();
        mailAnalysis.setSpamScore(Math.min(100, spamHits * 35));
        if (spamHits >= 3) {
            mailAnalysis.setSpamFlag(FLAG_YES);
            mailAnalysis.setSpamLevel(SPAM_LEVEL_HIGH);
            mailAnalysis.setSpamReason("命中多个垃圾邮件信号：" + formatMatches(spamMatches) + "。");
            return;
        }
        if (spamHits >= 2) {
            mailAnalysis.setSpamFlag(FLAG_YES);
            mailAnalysis.setSpamLevel(SPAM_LEVEL_MEDIUM);
            mailAnalysis.setSpamReason("命中多个营销或诱导点击信号：" + formatMatches(spamMatches) + "。");
            return;
        }
        if (spamHits == 1) {
            mailAnalysis.setSpamFlag(FLAG_YES);
            mailAnalysis.setSpamLevel(SPAM_LEVEL_LOW);
            mailAnalysis.setSpamReason("命中营销或垃圾邮件信号：" + formatMatches(spamMatches) + "。");
            return;
        }
        mailAnalysis.setSpamFlag(FLAG_NO);
        mailAnalysis.setSpamLevel(SPAM_LEVEL_NONE);
        mailAnalysis.setSpamReason("未发现中奖、广告、诱导点击或异常链接等垃圾邮件特征。");
    }

    private void applyRiskAnalysis(MailAnalysis mailAnalysis, List<String> riskMatches) {
        int riskHits = riskMatches.size();
        mailAnalysis.setRiskScore(Math.min(100, riskHits * 40));
        if (riskHits >= 2) {
            mailAnalysis.setRiskLevel(RISK_LEVEL_HIGH);
            mailAnalysis.setRiskReason("命中多个账号、敏感信息、付款或可疑链接风险信号：" + formatMatches(riskMatches) + "。");
            return;
        }
        if (riskHits == 1) {
            mailAnalysis.setRiskLevel(RISK_LEVEL_MEDIUM);
            mailAnalysis.setRiskReason("命中账号、敏感信息、付款或可疑链接风险信号：" + formatMatches(riskMatches) + "。");
            return;
        }
        mailAnalysis.setRiskLevel(RISK_LEVEL_SAFE);
        mailAnalysis.setRiskReason(null);
    }

    private boolean isCautiousReplyNeeded(MailAnalysis mailAnalysis) {
        return FLAG_YES == mailAnalysis.getSpamFlag()
                || RISK_LEVEL_MEDIUM.equals(mailAnalysis.getRiskLevel())
                || RISK_LEVEL_HIGH.equals(mailAnalysis.getRiskLevel());
    }

    private String buildSummary(String plainText) {
        String normalized = normalizeText(plainText);
        if (!StringUtils.hasText(normalized)) {
            return DEFAULT_SUMMARY;
        }
        if (normalized.length() <= SUMMARY_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, SUMMARY_MAX_LENGTH);
    }

    private String toJsonSuggestions(List<String> suggestions) {
        try {
            return objectMapper.writeValueAsString(suggestions);
        } catch (JsonProcessingException exception) {
            return "[\"收到，我会尽快处理。\",\"好的，如有问题我会及时反馈。\"]";
        }
    }

    private List<String> findKeywordHits(String text, List<String> keywords) {
        List<String> matches = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return matches;
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase(Locale.ROOT))) {
                matches.add(keyword);
            }
        }
        return matches;
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

    private String formatMatches(List<String> matches) {
        if (matches == null || matches.isEmpty()) {
            return "";
        }
        return String.join("、", matches);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("\\s+", " ").trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
