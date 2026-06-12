package com.scut.mailsystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserSettings {

    private Long id;
    private Long userId;
    private Integer aiEnabled;
    private Integer autoReplyEnabled;
    private Integer prioritySortEnabled;
    private String provider;
    private String baseUrl;
    private String modelName;
    private String apiKeyEncrypted;
    private String apiKeyMask;
    private Integer timeoutMs;
    private Integer maxTokens;
    private BigDecimal temperature;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(Integer aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public Integer getAutoReplyEnabled() {
        return autoReplyEnabled;
    }

    public void setAutoReplyEnabled(Integer autoReplyEnabled) {
        this.autoReplyEnabled = autoReplyEnabled;
    }

    public Integer getPrioritySortEnabled() {
        return prioritySortEnabled;
    }

    public void setPrioritySortEnabled(Integer prioritySortEnabled) {
        this.prioritySortEnabled = prioritySortEnabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public String getApiKeyEncrypted() {
        return apiKeyEncrypted;
    }

    public void setApiKeyEncrypted(String apiKeyEncrypted) {
        this.apiKeyEncrypted = apiKeyEncrypted;
    }

    public String getApiKeyMask() {
        return apiKeyMask;
    }

    public void setApiKeyMask(String apiKeyMask) {
        this.apiKeyMask = apiKeyMask;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
