package com.scut.mailsystem.vo.settings;

import java.math.BigDecimal;

public class UserSettingsVO {

    private Boolean aiEnabled;
    private Boolean autoReplyEnabled;
    private Boolean prioritySortEnabled;
    private Boolean modelConfigured;
    private String provider;
    private String modelName;
    private String baseUrl;
    private Boolean apiKeyConfigured;
    private String maskedApiKey;
    private Integer timeoutMs;
    private Integer maxTokens;
    private BigDecimal temperature;

    public Boolean getAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(Boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public Boolean getAutoReplyEnabled() {
        return autoReplyEnabled;
    }

    public void setAutoReplyEnabled(Boolean autoReplyEnabled) {
        this.autoReplyEnabled = autoReplyEnabled;
    }

    public Boolean getPrioritySortEnabled() {
        return prioritySortEnabled;
    }

    public void setPrioritySortEnabled(Boolean prioritySortEnabled) {
        this.prioritySortEnabled = prioritySortEnabled;
    }

    public Boolean getModelConfigured() {
        return modelConfigured;
    }

    public void setModelConfigured(Boolean modelConfigured) {
        this.modelConfigured = modelConfigured;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Boolean getApiKeyConfigured() {
        return apiKeyConfigured;
    }

    public void setApiKeyConfigured(Boolean apiKeyConfigured) {
        this.apiKeyConfigured = apiKeyConfigured;
    }

    public String getMaskedApiKey() {
        return maskedApiKey;
    }

    public void setMaskedApiKey(String maskedApiKey) {
        this.maskedApiKey = maskedApiKey;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
}
