package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.settings.UserSettingsUpdateRequest;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.entity.UserSettings;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.mapper.UserSettingsMapper;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.settings.UserSettingsVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSettingsServiceImplTest {

    private final UserSettingsMapper userSettingsMapper = mock(UserSettingsMapper.class);
    private final SysUserMapper sysUserMapper = mock(SysUserMapper.class);
    private final UserSettingsServiceImpl userSettingsService = new UserSettingsServiceImpl(
            userSettingsMapper,
            sysUserMapper
    );

    @Test
    void getSettings_withoutExistingRecord_createsDefaultSettings() {
        SysUser alice = activeUser(1L, "alice");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(userSettingsMapper.selectByUserId(1L)).thenReturn(null, defaultSettings(1L));

        UserSettingsVO settings = userSettingsService.getSettings(authHeader(1L, "alice"));

        assertFalse(settings.getAiEnabled());
        assertFalse(settings.getAutoReplyEnabled());
        assertTrue(settings.getPrioritySortEnabled());
        assertFalse(settings.getModelConfigured());
        assertFalse(settings.getApiKeyConfigured());
        assertEquals(10000, settings.getTimeoutMs());
        assertEquals(800, settings.getMaxTokens());
        assertEquals(new BigDecimal("0.20"), settings.getTemperature());
        verify(userSettingsMapper).insert(any(UserSettings.class));
    }

    @Test
    void updateSettings_onlyUpdatesProvidedSwitches() {
        SysUser alice = activeUser(1L, "alice");
        UserSettings existing = defaultSettings(1L);
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(userSettingsMapper.selectByUserId(1L)).thenReturn(existing, existing);

        UserSettingsUpdateRequest request = new UserSettingsUpdateRequest();
        request.setAiEnabled(true);
        request.setAutoReplyEnabled(true);

        UserSettingsVO settings = userSettingsService.updateSettings(authHeader(1L, "alice"), request);

        assertTrue(settings.getAiEnabled());
        assertTrue(settings.getAutoReplyEnabled());
        assertTrue(settings.getPrioritySortEnabled());
        assertFalse(settings.getModelConfigured());
        assertNull(settings.getProvider());
        verify(userSettingsMapper).updateByUserId(existing);
    }

    @Test
    void updateSettings_withModelConfigMasksApiKeyAndSetsModelConfigured() {
        SysUser alice = activeUser(1L, "alice");
        UserSettings existing = defaultSettings(1L);
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(userSettingsMapper.selectByUserId(1L)).thenReturn(existing, existing);

        UserSettingsUpdateRequest request = new UserSettingsUpdateRequest();
        request.setProvider("deepseek");
        request.setBaseUrl("https://api.deepseek.com");
        request.setModelName("deepseek-chat");
        request.setApiKey("sk-1234567890abcd");
        request.setTimeoutMs(10000);
        request.setMaxTokens(800);
        request.setTemperature(new BigDecimal("0.2"));

        UserSettingsVO settings = userSettingsService.updateSettings(authHeader(1L, "alice"), request);

        assertTrue(settings.getModelConfigured());
        assertTrue(settings.getApiKeyConfigured());
        assertEquals("deepseek", settings.getProvider());
        assertEquals("deepseek-chat", settings.getModelName());
        assertEquals("https://api.deepseek.com", settings.getBaseUrl());
        assertEquals("sk-****abcd", settings.getMaskedApiKey());
        assertEquals(new BigDecimal("0.20"), settings.getTemperature());

        ArgumentCaptor<UserSettings> captor = ArgumentCaptor.forClass(UserSettings.class);
        verify(userSettingsMapper).updateByUserId(captor.capture());
        UserSettings updated = captor.getValue();
        assertTrue(updated.getApiKeyEncrypted() != null && !updated.getApiKeyEncrypted().contains("sk-1234567890abcd"));
        assertEquals("sk-****abcd", updated.getApiKeyMask());
    }

    @Test
    void updateSettings_withEmptyApiKeyClearsSavedKey() {
        SysUser alice = activeUser(1L, "alice");
        UserSettings existing = configuredSettings(1L);
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(userSettingsMapper.selectByUserId(1L)).thenReturn(existing, existing);

        UserSettingsUpdateRequest request = new UserSettingsUpdateRequest();
        request.setApiKey("");

        UserSettingsVO settings = userSettingsService.updateSettings(authHeader(1L, "alice"), request);

        assertFalse(settings.getModelConfigured());
        assertFalse(settings.getApiKeyConfigured());
        assertNull(settings.getMaskedApiKey());
        assertNull(existing.getApiKeyEncrypted());
        assertNull(existing.getApiKeyMask());
    }

    @Test
    void updateSettings_rejectsInvalidProvider() {
        SysUser alice = activeUser(1L, "alice");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(userSettingsMapper.selectByUserId(1L)).thenReturn(defaultSettings(1L));

        UserSettingsUpdateRequest request = new UserSettingsUpdateRequest();
        request.setProvider("bad-provider");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userSettingsService.updateSettings(authHeader(1L, "alice"), request)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertEquals("不支持的 AI 服务商", exception.getMessage());
    }

    @Test
    void updateSettings_rejectsTemperatureOutOfCurrentApiRange() {
        SysUser alice = activeUser(1L, "alice");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(userSettingsMapper.selectByUserId(1L)).thenReturn(defaultSettings(1L));

        UserSettingsUpdateRequest request = new UserSettingsUpdateRequest();
        request.setTemperature(new BigDecimal("1.1"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userSettingsService.updateSettings(authHeader(1L, "alice"), request)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertEquals("temperature 参数不合法", exception.getMessage());
    }

    @Test
    void getSettings_withInvalidTokenReturnsUnauthorized() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userSettingsService.getSettings("Bearer invalid-token")
        );

        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
    }

    private UserSettings defaultSettings(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setAiEnabled(0);
        settings.setAutoReplyEnabled(0);
        settings.setPrioritySortEnabled(1);
        settings.setTimeoutMs(10000);
        settings.setMaxTokens(800);
        settings.setTemperature(new BigDecimal("0.20"));
        return settings;
    }

    private UserSettings configuredSettings(Long userId) {
        UserSettings settings = defaultSettings(userId);
        settings.setProvider("deepseek");
        settings.setBaseUrl("https://api.deepseek.com");
        settings.setModelName("deepseek-chat");
        settings.setApiKeyEncrypted("encrypted-value");
        settings.setApiKeyMask("sk-****abcd");
        return settings;
    }

    private SysUser activeUser(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        return user;
    }

    private String authHeader(Long userId, String username) {
        return "Bearer " + TokenUtils.generateToken(userId, username);
    }
}
