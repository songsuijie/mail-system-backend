package com.scut.mailsystem.service.ai.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.config.AiDefaultModelProperties;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.entity.UserSettings;
import com.scut.mailsystem.mapper.UserSettingsMapper;
import com.scut.mailsystem.service.ai.AiAnalysisService;
import com.scut.mailsystem.service.ai.RuleAnalysisService;
import com.scut.mailsystem.utils.ApiKeyCryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final int FLAG_NO = 0;
    private static final int FLAG_YES = 1;
    private static final String ANALYSIS_STATUS_SUCCESS = "SUCCESS";
    private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");
    private static final Set<String> SPAM_LEVELS = Set.of("NONE", "LOW", "MEDIUM", "HIGH");
    private static final Set<String> RISK_LEVELS = Set.of("SAFE", "LOW", "MEDIUM", "HIGH");
    private static final String SYSTEM_PROMPT = "你是邮件分析助手。你只能输出 JSON，不要输出 Markdown，不要解释。"
            + "priority 只能是 LOW、MEDIUM、HIGH。"
            + "spamLevel 只能是 NONE、LOW、MEDIUM、HIGH。"
            + "riskLevel 只能是 SAFE、LOW、MEDIUM、HIGH。"
            + "replySuggestions 返回 2 条中文短回复。";

    private final RuleAnalysisService ruleAnalysisService;
    private final UserSettingsMapper userSettingsMapper;
    private final AiDefaultModelProperties defaultModelProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public AiAnalysisServiceImpl(RuleAnalysisService ruleAnalysisService,
                                 UserSettingsMapper userSettingsMapper,
                                 AiDefaultModelProperties defaultModelProperties,
                                 ObjectMapper objectMapper) {
        this(ruleAnalysisService, userSettingsMapper, defaultModelProperties, objectMapper, HttpClient.newHttpClient());
    }

    AiAnalysisServiceImpl(RuleAnalysisService ruleAnalysisService,
                          UserSettingsMapper userSettingsMapper,
                          AiDefaultModelProperties defaultModelProperties,
                          ObjectMapper objectMapper,
                          HttpClient httpClient) {
        this.ruleAnalysisService = ruleAnalysisService;
        this.userSettingsMapper = userSettingsMapper;
        this.defaultModelProperties = defaultModelProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public MailAnalysis analyze(Long mailId, Long recipientId, String subject, String content, LocalDateTime now) {
        MailAnalysis fallback = ruleAnalysisService.analyze(mailId, recipientId, subject, content, now);
        ModelConfig modelConfig = chooseModelConfig(recipientId);
        if (modelConfig == null) {
            return fallback;
        }

        try {
            MailAnalysis analysis = callModel(modelConfig, mailId, recipientId, subject, content, now);
            analysis.setAiProvider(modelConfig.provider());
            analysis.setModelName(modelConfig.modelName());
            analysis.setAiErrorMessage(null);
            return analysis;
        } catch (RuntimeException exception) {
            fallback.setAiProvider(modelConfig.provider());
            fallback.setModelName(modelConfig.modelName());
            fallback.setAiErrorMessage(shortError(exception.getMessage()));
            fallback.setUpdatedAt(now);
            return fallback;
        }
    }

    private ModelConfig chooseModelConfig(Long recipientId) {
        UserSettings settings = userSettingsMapper.selectByUserId(recipientId);
        if (settings != null && Integer.valueOf(0).equals(settings.getAiEnabled())) {
            return null;
        }
        if (settings != null && Integer.valueOf(1).equals(settings.getAiEnabled())) {
            ModelConfig userModel = userModelConfig(settings);
            if (userModel != null) {
                return userModel;
            }
        }
        if (settings == null || Integer.valueOf(1).equals(settings.getAiEnabled())) {
            return defaultModelConfig();
        }
        return null;
    }

    private ModelConfig userModelConfig(UserSettings settings) {
        if (!StringUtils.hasText(settings.getProvider())
                || !StringUtils.hasText(settings.getBaseUrl())
                || !StringUtils.hasText(settings.getModelName())
                || !StringUtils.hasText(settings.getApiKeyEncrypted())) {
            return null;
        }
        String apiKey = ApiKeyCryptoUtils.decodeApiKey(settings.getApiKeyEncrypted());
        if (!StringUtils.hasText(apiKey)) {
            return null;
        }
        return new ModelConfig(
                settings.getProvider().trim(),
                settings.getBaseUrl().trim(),
                settings.getModelName().trim(),
                apiKey,
                defaultIfNull(settings.getTimeoutMs(), 10000),
                defaultIfNull(settings.getMaxTokens(), 800),
                settings.getTemperature() == null ? new BigDecimal("0.20") : settings.getTemperature()
        );
    }

    private ModelConfig defaultModelConfig() {
        if (!defaultModelProperties.isComplete()) {
            return null;
        }
        return new ModelConfig(
                defaultModelProperties.getProvider().trim(),
                defaultModelProperties.getBaseUrl().trim(),
                defaultModelProperties.getModelName().trim(),
                defaultModelProperties.getApiKey().trim(),
                defaultModelProperties.getTimeoutMs(),
                defaultModelProperties.getMaxTokens(),
                defaultModelProperties.getTemperature() == null
                        ? new BigDecimal("0.20")
                        : defaultModelProperties.getTemperature()
        );
    }

    private MailAnalysis callModel(ModelConfig modelConfig,
                                   Long mailId,
                                   Long recipientId,
                                   String subject,
                                   String content,
                                   LocalDateTime now) {
        String requestBody = buildRequestBody(modelConfig, subject, content);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(modelEndpoint(modelConfig.baseUrl())))
                .timeout(Duration.ofMillis(Math.max(1000, modelConfig.timeoutMs())))
                .header("Authorization", "Bearer " + modelConfig.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new IllegalStateException("模型请求失败");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("模型请求被中断");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("模型返回 HTTP " + response.statusCode());
        }

        String modelContent = extractModelContent(response.body());
        return parseModelAnalysis(modelContent, mailId, recipientId, now);
    }

    private String buildRequestBody(ModelConfig modelConfig, String subject, String content) {
        Map<String, Object> request = Map.of(
                "model", modelConfig.modelName(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", buildUserPrompt(subject, content))
                ),
                "temperature", modelConfig.temperature(),
                "max_tokens", modelConfig.maxTokens()
        );
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("模型请求体生成失败");
        }
    }

    private String buildUserPrompt(String subject, String content) {
        return "邮件主题：" + defaultString(subject)
                + "\n\n邮件正文：" + defaultString(content)
                + "\n\n请只输出 JSON，格式如下："
                + "{\"priority\":\"HIGH\",\"priorityReason\":\"邮件包含截止时间和提交要求\","
                + "\"spam\":false,\"spamLevel\":\"NONE\",\"spamReason\":\"未发现垃圾邮件特征\","
                + "\"riskLevel\":\"SAFE\",\"riskReason\":\"\",\"summary\":\"提醒收件人按时处理邮件。\","
                + "\"replySuggestions\":[\"收到，我会按时处理。\",\"好的，我会尽快处理。\"]}";
    }

    private String extractModelContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isTextual() || !StringUtils.hasText(contentNode.asText())) {
                throw new IllegalStateException("模型响应缺少内容");
            }
            return contentNode.asText();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("模型响应不是合法 JSON");
        }
    }

    private MailAnalysis parseModelAnalysis(String modelContent, Long mailId, Long recipientId, LocalDateTime now) {
        JsonNode root;
        try {
            root = objectMapper.readTree(stripJsonFence(modelContent));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("模型输出不是合法 JSON");
        }

        String priority = requiredText(root, "priority");
        String spamLevel = requiredText(root, "spamLevel");
        String riskLevel = requiredText(root, "riskLevel");
        if (!PRIORITIES.contains(priority) || !SPAM_LEVELS.contains(spamLevel) || !RISK_LEVELS.contains(riskLevel)) {
            throw new IllegalStateException("模型输出枚举值不合法");
        }

        List<String> suggestions = parseSuggestions(root.path("replySuggestions"));
        MailAnalysis analysis = new MailAnalysis();
        analysis.setMailId(mailId);
        analysis.setRecipientId(recipientId);
        analysis.setAnalysisStatus(ANALYSIS_STATUS_SUCCESS);
        analysis.setPriority(priority);
        analysis.setPriorityScore(scoreByPriority(priority));
        analysis.setPriorityReason(textOrDefault(root, "priorityReason", "模型判断邮件优先级。"));
        analysis.setSpamFlag(root.path("spam").asBoolean(!"NONE".equals(spamLevel)) ? FLAG_YES : FLAG_NO);
        analysis.setSpamScore(scoreBySpamLevel(spamLevel));
        analysis.setSpamLevel(spamLevel);
        analysis.setSpamReason(textOrDefault(root, "spamReason", ""));
        analysis.setRiskLevel(riskLevel);
        analysis.setRiskScore(scoreByRiskLevel(riskLevel));
        analysis.setRiskReason(textOrDefault(root, "riskReason", null));
        analysis.setSummary(textOrDefault(root, "summary", ""));
        analysis.setReplySuggestions(toJsonSuggestions(suggestions));
        analysis.setCreatedAt(now);
        analysis.setUpdatedAt(now);
        return analysis;
    }

    private List<String> parseSuggestions(JsonNode node) {
        if (!node.isArray() || node.size() == 0) {
            throw new IllegalStateException("模型输出缺少回复建议");
        }
        List<String> suggestions = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual() && StringUtils.hasText(item.asText())) {
                suggestions.add(item.asText());
            }
        }
        if (suggestions.isEmpty()) {
            throw new IllegalStateException("模型输出回复建议不合法");
        }
        return suggestions.size() > 2 ? suggestions.subList(0, 2) : suggestions;
    }

    private String toJsonSuggestions(List<String> suggestions) {
        try {
            return objectMapper.writeValueAsString(suggestions);
        } catch (JsonProcessingException exception) {
            return "[\"收到，我会尽快处理。\",\"好的，如有问题我会及时反馈。\"]";
        }
    }

    private String requiredText(JsonNode root, String fieldName) {
        JsonNode node = root.path(fieldName);
        if (!node.isTextual() || !StringUtils.hasText(node.asText())) {
            throw new IllegalStateException("模型输出缺少字段：" + fieldName);
        }
        return node.asText().trim();
    }

    private String textOrDefault(JsonNode root, String fieldName, String defaultValue) {
        JsonNode node = root.path(fieldName);
        return node.isTextual() && StringUtils.hasText(node.asText()) ? node.asText().trim() : defaultValue;
    }

    private String stripJsonFence(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return trimmed.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private String modelEndpoint(String baseUrl) {
        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private int scoreByPriority(String priority) {
        if ("HIGH".equals(priority)) {
            return 85;
        }
        if ("LOW".equals(priority)) {
            return 25;
        }
        return 50;
    }

    private int scoreBySpamLevel(String spamLevel) {
        if ("HIGH".equals(spamLevel)) {
            return 90;
        }
        if ("MEDIUM".equals(spamLevel)) {
            return 60;
        }
        if ("LOW".equals(spamLevel)) {
            return 30;
        }
        return 0;
    }

    private int scoreByRiskLevel(String riskLevel) {
        if ("HIGH".equals(riskLevel)) {
            return 90;
        }
        if ("MEDIUM".equals(riskLevel)) {
            return 60;
        }
        if ("LOW".equals(riskLevel)) {
            return 30;
        }
        return 0;
    }

    private String shortError(String message) {
        if (!StringUtils.hasText(message)) {
            return "AI 分析失败，已使用规则分析结果。";
        }
        String normalized = message.replaceAll("\\s+", " ").trim();
        return normalized.length() > 200 ? normalized.substring(0, 200) : normalized;
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private record ModelConfig(String provider,
                               String baseUrl,
                               String modelName,
                               String apiKey,
                               int timeoutMs,
                               int maxTokens,
                               BigDecimal temperature) {
    }
}
