package com.scut.mailsystem.dto.settings;

import java.math.BigDecimal;

public class UserSettingsUpdateRequest {

    private Boolean aiEnabled;
    private Boolean autoReplyEnabled;
    private Boolean prioritySortEnabled;
    private String provider;
    private String apiKey;
    private String baseUrl;
    private String modelName;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
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
