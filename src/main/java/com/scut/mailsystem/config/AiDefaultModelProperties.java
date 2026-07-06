package com.scut.mailsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "ai.default-model")
public class AiDefaultModelProperties {

    private boolean enabled = true;
    private String provider = "qwen";
    private String baseUrl = "https://llm-dujne1fkrf6fj1k5.cn-beijing.maas.aliyuncs.com/compatible-mode/v1";
    private String modelName = "qwen3.6-flash";
    private String apiKey;
    private int timeoutMs = 10000;
    private int maxTokens = 800;
    private BigDecimal temperature = new BigDecimal("0.20");

    public boolean isComplete() {
        return enabled
                && StringUtils.hasText(provider)
                && StringUtils.hasText(baseUrl)
                && StringUtils.hasText(modelName)
                && StringUtils.hasText(apiKey);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
}
