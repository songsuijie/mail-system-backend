package com.scut.mailsystem.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.mailsystem.config.AiDefaultModelProperties;
import com.scut.mailsystem.entity.MailAnalysis;
import com.scut.mailsystem.entity.UserSettings;
import com.scut.mailsystem.mapper.UserSettingsMapper;
import com.scut.mailsystem.service.ai.RuleAnalysisService;
import com.scut.mailsystem.utils.ApiKeyCryptoUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalysisServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RuleAnalysisService ruleAnalysisService = new RuleAnalysisServiceImpl(objectMapper);
    private final UserSettingsMapper userSettingsMapper = mock(UserSettingsMapper.class);
    private final AiDefaultModelProperties defaultModelProperties = new AiDefaultModelProperties();
    private final HttpClient httpClient = mock(HttpClient.class);

    @Test
    void analyze_userDisabledAi_returnsRuleAnalysisWithoutCallingModel() throws Exception {
        when(userSettingsMapper.selectByUserId(2L)).thenReturn(settings(2L, 0, null, null, null, null));
        AiAnalysisServiceImpl service = service();

        MailAnalysis analysis = service.analyze(100L, 2L, "普通通知", richText("请提交报告"), now());

        assertEquals("SUCCESS", analysis.getAnalysisStatus());
        assertEquals("请提交报告", analysis.getSummary());
        assertNull(analysis.getAiProvider());
        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void analyze_userModelComplete_prefersUserModel() throws Exception {
        when(userSettingsMapper.selectByUserId(2L)).thenReturn(settings(
                2L,
                1,
                "deepseek",
                "https://user.example/v1",
                "user-model",
                ApiKeyCryptoUtils.encodeApiKey("sk-user-test")
        ));
        whenModelReturns(200, modelResponse("{\"priority\":\"HIGH\",\"priorityReason\":\"模型判断紧急\","
                + "\"spam\":false,\"spamLevel\":\"NONE\",\"spamReason\":\"未发现垃圾邮件特征\","
                + "\"riskLevel\":\"SAFE\",\"riskReason\":\"\",\"summary\":\"模型摘要\","
                + "\"replySuggestions\":[\"收到，我会处理。\",\"好的，我会跟进。\"]}"));

        MailAnalysis analysis = service().analyze(100L, 2L, "紧急通知", richText("请今天处理"), now());

        assertEquals("HIGH", analysis.getPriority());
        assertEquals("模型摘要", analysis.getSummary());
        assertEquals("deepseek", analysis.getAiProvider());
        assertEquals("user-model", analysis.getModelName());
        assertNull(analysis.getAiErrorMessage());
    }

    @Test
    void analyze_userModelIncomplete_usesDefaultModelWhenConfigured() throws Exception {
        when(userSettingsMapper.selectByUserId(2L)).thenReturn(settings(2L, 1, "qwen", null, "bad", null));
        configureDefaultModel("sk-default-test");
        whenModelReturns(200, modelResponse("{\"priority\":\"MEDIUM\",\"priorityReason\":\"普通邮件\","
                + "\"spam\":false,\"spamLevel\":\"NONE\",\"spamReason\":\"未发现垃圾邮件特征\","
                + "\"riskLevel\":\"SAFE\",\"riskReason\":\"\",\"summary\":\"默认模型摘要\","
                + "\"replySuggestions\":[\"收到。\",\"好的。\"]}"));

        MailAnalysis analysis = service().analyze(100L, 2L, "通知", richText("请查收"), now());

        assertEquals("qwen", analysis.getAiProvider());
        assertEquals("qwen3.6-flash", analysis.getModelName());
        assertEquals("默认模型摘要", analysis.getSummary());
    }

    @Test
    void analyze_defaultModelWithoutApiKey_returnsRuleAnalysis() throws Exception {
        when(userSettingsMapper.selectByUserId(2L)).thenReturn(null);
        defaultModelProperties.setApiKey("");

        MailAnalysis analysis = service().analyze(100L, 2L, "普通通知", richText("请提交报告"), now());

        assertEquals("请提交报告", analysis.getSummary());
        assertNull(analysis.getAiProvider());
        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void analyze_modelReturnsNonJson_fallsBackAndRecordsError() throws Exception {
        when(userSettingsMapper.selectByUserId(2L)).thenReturn(settings(
                2L,
                1,
                "deepseek",
                "https://user.example/v1",
                "user-model",
                ApiKeyCryptoUtils.encodeApiKey("sk-user-test")
        ));
        whenModelReturns(200, modelResponse("not-json"));

        MailAnalysis analysis = service().analyze(100L, 2L, "紧急通知", richText("请今天提交报告"), now());

        assertEquals("HIGH", analysis.getPriority());
        assertEquals("deepseek", analysis.getAiProvider());
        assertEquals("user-model", analysis.getModelName());
        assertNotNull(analysis.getAiErrorMessage());
        assertTrue(analysis.getAiErrorMessage().contains("JSON"));
    }

    @Test
    void analyze_modelReturnsHttpError_fallsBackAndRecordsError() throws Exception {
        when(userSettingsMapper.selectByUserId(2L)).thenReturn(settings(
                2L,
                1,
                "deepseek",
                "https://user.example/v1",
                "user-model",
                ApiKeyCryptoUtils.encodeApiKey("sk-user-test")
        ));
        whenModelReturns(500, "{\"error\":\"failed\"}");

        MailAnalysis analysis = service().analyze(100L, 2L, "普通通知", richText("请提交报告"), now());

        assertEquals("请提交报告", analysis.getSummary());
        assertEquals("deepseek", analysis.getAiProvider());
        assertTrue(analysis.getAiErrorMessage().contains("HTTP 500"));
    }

    @Test
    void apiKeyCrypto_roundTripsAndMasksWithoutPlainTextLeak() {
        String encoded = ApiKeyCryptoUtils.encodeApiKey("sk-1234567890abcd");

        assertEquals("sk-1234567890abcd", ApiKeyCryptoUtils.decodeApiKey(encoded));
        assertTrue(!encoded.contains("sk-1234567890abcd"));
        assertEquals("sk-****abcd", ApiKeyCryptoUtils.maskApiKey("sk-1234567890abcd"));
    }

    private AiAnalysisServiceImpl service() {
        return new AiAnalysisServiceImpl(
                ruleAnalysisService,
                userSettingsMapper,
                defaultModelProperties,
                objectMapper,
                httpClient
        );
    }

    private void configureDefaultModel(String apiKey) {
        defaultModelProperties.setEnabled(true);
        defaultModelProperties.setProvider("qwen");
        defaultModelProperties.setBaseUrl("https://default.example/v1");
        defaultModelProperties.setModelName("qwen3.6-flash");
        defaultModelProperties.setApiKey(apiKey);
    }

    @SuppressWarnings("unchecked")
    private void whenModelReturns(int statusCode, String body) throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
    }

    private String modelResponse(String content) throws IOException {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "choices",
                java.util.List.of(java.util.Map.of(
                        "message",
                        java.util.Map.of("content", content)
                ))
        ));
    }

    private UserSettings settings(Long userId,
                                  Integer aiEnabled,
                                  String provider,
                                  String baseUrl,
                                  String modelName,
                                  String apiKeyEncrypted) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setAiEnabled(aiEnabled);
        settings.setProvider(provider);
        settings.setBaseUrl(baseUrl);
        settings.setModelName(modelName);
        settings.setApiKeyEncrypted(apiKeyEncrypted);
        settings.setTimeoutMs(10000);
        settings.setMaxTokens(800);
        settings.setTemperature(new BigDecimal("0.20"));
        return settings;
    }

    private String richText(String text) {
        return "[{\"type\":\"paragraph\",\"children\":[{\"type\":\"text\",\"text\":\"" + text + "\"}]}]";
    }

    private LocalDateTime now() {
        return LocalDateTime.of(2026, 7, 6, 12, 0);
    }
}
