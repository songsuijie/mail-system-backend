package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.settings.UserSettingsUpdateRequest;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.entity.UserSettings;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.mapper.UserSettingsMapper;
import com.scut.mailsystem.service.settings.UserSettingsService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.settings.UserSettingsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final int DEFAULT_AI_ENABLED = 0;
    private static final int DEFAULT_AUTO_REPLY_ENABLED = 0;
    private static final int DEFAULT_PRIORITY_SORT_ENABLED = 1;
    private static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int DEFAULT_MAX_TOKENS = 800;
    private static final BigDecimal DEFAULT_TEMPERATURE = new BigDecimal("0.20");
    private static final int MIN_TIMEOUT_MS = 3000;
    private static final int MAX_TIMEOUT_MS = 30000;
    private static final int MIN_MAX_TOKENS = 100;
    private static final int MAX_MAX_TOKENS = 4000;
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("0");
    private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("1");
    private static final byte[] API_KEY_ENCODE_SECRET = "mail-system-api-key-dev-secret".getBytes(StandardCharsets.UTF_8);
    private static final Set<String> SUPPORTED_PROVIDERS = Set.of(
            "qwen", "openai", "deepseek", "kimi", "glm", "siliconflow", "custom"
    );

    private final UserSettingsMapper userSettingsMapper;
    private final SysUserMapper sysUserMapper;

    public UserSettingsServiceImpl(UserSettingsMapper userSettingsMapper, SysUserMapper sysUserMapper) {
        this.userSettingsMapper = userSettingsMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional
    public UserSettingsVO getSettings(String authorizationHeader) {
        Long userId = getCurrentUserId(authorizationHeader);
        UserSettings settings = getOrCreateSettings(userId);
        return toVO(settings);
    }

    @Override
    @Transactional
    public UserSettingsVO updateSettings(String authorizationHeader, UserSettingsUpdateRequest request) {
        Long userId = getCurrentUserId(authorizationHeader);
        UserSettings settings = getOrCreateSettings(userId);
        if (request == null) {
            return toVO(settings);
        }

        applyBooleanFields(settings, request);
        applyModelFields(settings, request);
        applyNumberFields(settings, request);
        settings.setUpdatedAt(LocalDateTime.now());
        userSettingsMapper.updateByUserId(settings);

        UserSettings updated = userSettingsMapper.selectByUserId(userId);
        return toVO(updated);
    }

    private Long getCurrentUserId(String authorizationHeader) {
        String token = AuthHeaderUtils.extractToken(authorizationHeader);
        LoginUser loginUser = TokenUtils.parseToken(token);
        SysUser user = sysUserMapper.selectActiveById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user.getId();
    }

    private UserSettings getOrCreateSettings(Long userId) {
        UserSettings settings = userSettingsMapper.selectByUserId(userId);
        if (settings != null) {
            normalizeDefaults(settings);
            return settings;
        }

        UserSettings defaultSettings = new UserSettings();
        defaultSettings.setUserId(userId);
        defaultSettings.setAiEnabled(DEFAULT_AI_ENABLED);
        defaultSettings.setAutoReplyEnabled(DEFAULT_AUTO_REPLY_ENABLED);
        defaultSettings.setPrioritySortEnabled(DEFAULT_PRIORITY_SORT_ENABLED);
        defaultSettings.setTimeoutMs(DEFAULT_TIMEOUT_MS);
        defaultSettings.setMaxTokens(DEFAULT_MAX_TOKENS);
        defaultSettings.setTemperature(DEFAULT_TEMPERATURE);
        LocalDateTime now = LocalDateTime.now();
        defaultSettings.setCreatedAt(now);
        defaultSettings.setUpdatedAt(now);
        userSettingsMapper.insert(defaultSettings);
        return userSettingsMapper.selectByUserId(userId);
    }

    private void normalizeDefaults(UserSettings settings) {
        if (settings.getAiEnabled() == null) {
            settings.setAiEnabled(DEFAULT_AI_ENABLED);
        }
        if (settings.getAutoReplyEnabled() == null) {
            settings.setAutoReplyEnabled(DEFAULT_AUTO_REPLY_ENABLED);
        }
        if (settings.getPrioritySortEnabled() == null) {
            settings.setPrioritySortEnabled(DEFAULT_PRIORITY_SORT_ENABLED);
        }
        if (settings.getTimeoutMs() == null) {
            settings.setTimeoutMs(DEFAULT_TIMEOUT_MS);
        }
        if (settings.getMaxTokens() == null) {
            settings.setMaxTokens(DEFAULT_MAX_TOKENS);
        }
        if (settings.getTemperature() == null) {
            settings.setTemperature(DEFAULT_TEMPERATURE);
        }
    }

    private void applyBooleanFields(UserSettings settings, UserSettingsUpdateRequest request) {
        if (request.getAiEnabled() != null) {
            settings.setAiEnabled(toTinyInt(request.getAiEnabled()));
        }
        if (request.getAutoReplyEnabled() != null) {
            settings.setAutoReplyEnabled(toTinyInt(request.getAutoReplyEnabled()));
        }
        if (request.getPrioritySortEnabled() != null) {
            settings.setPrioritySortEnabled(toTinyInt(request.getPrioritySortEnabled()));
        }
    }

    private void applyModelFields(UserSettings settings, UserSettingsUpdateRequest request) {
        if (request.getProvider() != null) {
            String provider = trimToNull(request.getProvider());
            if (provider == null || !SUPPORTED_PROVIDERS.contains(provider)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的 AI 服务商");
            }
            settings.setProvider(provider);
        }

        if (request.getBaseUrl() != null) {
            String baseUrl = trimToNull(request.getBaseUrl());
            if (baseUrl == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "Base URL 不能为空");
            }
            if (!(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "Base URL 格式不合法");
            }
            settings.setBaseUrl(baseUrl);
        }

        if (request.getModelName() != null) {
            String modelName = trimToNull(request.getModelName());
            if (modelName == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "模型名称不能为空");
            }
            settings.setModelName(modelName);
        }

        if (request.getApiKey() != null) {
            String apiKey = request.getApiKey();
            if (apiKey.isEmpty()) {
                settings.setApiKeyEncrypted(null);
                settings.setApiKeyMask(null);
            } else {
                String trimmedApiKey = trimToNull(apiKey);
                if (trimmedApiKey == null) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "API Key 不能为空");
                }
                settings.setApiKeyEncrypted(encodeApiKey(trimmedApiKey));
                settings.setApiKeyMask(maskApiKey(trimmedApiKey));
            }
        }
    }

    private void applyNumberFields(UserSettings settings, UserSettingsUpdateRequest request) {
        if (request.getTimeoutMs() != null) {
            if (request.getTimeoutMs() < MIN_TIMEOUT_MS || request.getTimeoutMs() > MAX_TIMEOUT_MS) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "timeoutMs 参数不合法");
            }
            settings.setTimeoutMs(request.getTimeoutMs());
        }

        if (request.getMaxTokens() != null) {
            if (request.getMaxTokens() < MIN_MAX_TOKENS || request.getMaxTokens() > MAX_MAX_TOKENS) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "maxTokens 参数不合法");
            }
            settings.setMaxTokens(request.getMaxTokens());
        }

        if (request.getTemperature() != null) {
            BigDecimal temperature = request.getTemperature();
            if (temperature.compareTo(MIN_TEMPERATURE) < 0 || temperature.compareTo(MAX_TEMPERATURE) > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "temperature 参数不合法");
            }
            settings.setTemperature(temperature.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private UserSettingsVO toVO(UserSettings settings) {
        normalizeDefaults(settings);
        boolean apiKeyConfigured = StringUtils.hasText(settings.getApiKeyEncrypted());
        boolean modelConfigured = StringUtils.hasText(settings.getProvider())
                && StringUtils.hasText(settings.getBaseUrl())
                && StringUtils.hasText(settings.getModelName())
                && apiKeyConfigured;

        UserSettingsVO vo = new UserSettingsVO();
        vo.setAiEnabled(toBoolean(settings.getAiEnabled()));
        vo.setAutoReplyEnabled(toBoolean(settings.getAutoReplyEnabled()));
        vo.setPrioritySortEnabled(toBoolean(settings.getPrioritySortEnabled()));
        vo.setModelConfigured(modelConfigured);
        vo.setProvider(trimToNull(settings.getProvider()));
        vo.setModelName(trimToNull(settings.getModelName()));
        vo.setBaseUrl(trimToNull(settings.getBaseUrl()));
        vo.setApiKeyConfigured(apiKeyConfigured);
        vo.setMaskedApiKey(settings.getApiKeyMask());
        vo.setTimeoutMs(settings.getTimeoutMs());
        vo.setMaxTokens(settings.getMaxTokens());
        vo.setTemperature(settings.getTemperature());
        return vo;
    }

    private Integer toTinyInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private Boolean toBoolean(Integer value) {
        return value != null && value == 1;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String encodeApiKey(String apiKey) {
        byte[] bytes = apiKey.getBytes(StandardCharsets.UTF_8);
        byte[] encodedBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            encodedBytes[i] = (byte) (bytes[i] ^ API_KEY_ENCODE_SECRET[i % API_KEY_ENCODE_SECRET.length]);
        }
        return Base64.getEncoder().encodeToString(encodedBytes);
    }

    private String maskApiKey(String apiKey) {
        String trimmed = apiKey.trim();
        if (trimmed.length() >= 7 && trimmed.startsWith("sk-")) {
            return "sk-****" + trimmed.substring(trimmed.length() - 4);
        }
        if (trimmed.length() >= 4) {
            return "****" + trimmed.substring(trimmed.length() - 2);
        }
        return "****";
    }
}
