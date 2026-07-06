package com.scut.mailsystem.controller.user;

import com.scut.mailsystem.dto.settings.UserSettingsUpdateRequest;
import com.scut.mailsystem.dto.user.ChangePasswordRequest;
import com.scut.mailsystem.service.settings.UserSettingsService;
import com.scut.mailsystem.service.user.UserService;
import com.scut.mailsystem.vo.settings.UserSettingsVO;
import com.scut.mailsystem.vo.user.CurrentUserVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final UserSettingsService userSettingsService = mock(UserSettingsService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UserController(userService, userSettingsService))
            .build();

    @Test
    void getCurrentUser_returnsFinalContractUserFields() throws Exception {
        when(userService.getCurrentUser("Bearer token"))
                .thenReturn(new CurrentUserVO("admin", "管理员", "admin@mail.com", "管"));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.nickname").value("管理员"))
                .andExpect(jsonPath("$.data.emailAddress").value("admin@mail.com"))
                .andExpect(jsonPath("$.data.avatarText").value("管"));

        verify(userService).getCurrentUser("Bearer token");
    }

    @Test
    void changePassword_passesAuthorizationAndBodyToService() throws Exception {
        mockMvc.perform(put("/api/users/password")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "old123456",
                                  "newPassword": "new123456",
                                  "confirmPassword": "new123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"));

        ArgumentCaptor<ChangePasswordRequest> captor = ArgumentCaptor.forClass(ChangePasswordRequest.class);
        verify(userService).changePassword(org.mockito.ArgumentMatchers.eq("Bearer token"), captor.capture());
        assertEquals("old123456", captor.getValue().getOldPassword());
        assertEquals("new123456", captor.getValue().getNewPassword());
        assertEquals("new123456", captor.getValue().getConfirmPassword());
    }

    @Test
    void getSettings_returnsModelConfigurationFields() throws Exception {
        when(userSettingsService.getSettings("Bearer token")).thenReturn(settings());

        mockMvc.perform(get("/api/users/settings")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.aiEnabled").value(true))
                .andExpect(jsonPath("$.data.autoReplyEnabled").value(false))
                .andExpect(jsonPath("$.data.prioritySortEnabled").value(true))
                .andExpect(jsonPath("$.data.modelConfigured").value(true))
                .andExpect(jsonPath("$.data.provider").value("openai-compatible"))
                .andExpect(jsonPath("$.data.modelName").value("deepseek-chat"))
                .andExpect(jsonPath("$.data.baseUrl").value("https://api.deepseek.com/v1"))
                .andExpect(jsonPath("$.data.apiKeyConfigured").value(true))
                .andExpect(jsonPath("$.data.maskedApiKey").value("sk-***-1234"))
                .andExpect(jsonPath("$.data.timeoutMs").value(10000))
                .andExpect(jsonPath("$.data.maxTokens").value(800))
                .andExpect(jsonPath("$.data.temperature").value(0.2));

        verify(userSettingsService).getSettings("Bearer token");
    }

    @Test
    void updateSettings_passesFinalContractFieldsToService() throws Exception {
        when(userSettingsService.updateSettings(
                org.mockito.ArgumentMatchers.eq("Bearer token"),
                any(UserSettingsUpdateRequest.class)
        )).thenReturn(settings());

        mockMvc.perform(put("/api/users/settings")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "aiEnabled": true,
                                  "autoReplyEnabled": false,
                                  "prioritySortEnabled": true,
                                  "provider": "openai-compatible",
                                  "baseUrl": "https://api.deepseek.com/v1",
                                  "modelName": "deepseek-chat",
                                  "apiKey": "sk-test",
                                  "timeoutMs": 10000,
                                  "maxTokens": 800,
                                  "temperature": 0.2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.modelConfigured").value(true));

        ArgumentCaptor<UserSettingsUpdateRequest> captor =
                ArgumentCaptor.forClass(UserSettingsUpdateRequest.class);
        verify(userSettingsService).updateSettings(
                org.mockito.ArgumentMatchers.eq("Bearer token"),
                captor.capture()
        );
        assertEquals(true, captor.getValue().getAiEnabled());
        assertEquals(false, captor.getValue().getAutoReplyEnabled());
        assertEquals(true, captor.getValue().getPrioritySortEnabled());
        assertEquals("openai-compatible", captor.getValue().getProvider());
        assertEquals("https://api.deepseek.com/v1", captor.getValue().getBaseUrl());
        assertEquals("deepseek-chat", captor.getValue().getModelName());
        assertEquals("sk-test", captor.getValue().getApiKey());
        assertEquals(10000, captor.getValue().getTimeoutMs());
        assertEquals(800, captor.getValue().getMaxTokens());
        assertEquals(0, new BigDecimal("0.2").compareTo(captor.getValue().getTemperature()));
    }

    private UserSettingsVO settings() {
        UserSettingsVO settings = new UserSettingsVO();
        settings.setAiEnabled(true);
        settings.setAutoReplyEnabled(false);
        settings.setPrioritySortEnabled(true);
        settings.setModelConfigured(true);
        settings.setProvider("openai-compatible");
        settings.setModelName("deepseek-chat");
        settings.setBaseUrl("https://api.deepseek.com/v1");
        settings.setApiKeyConfigured(true);
        settings.setMaskedApiKey("sk-***-1234");
        settings.setTimeoutMs(10000);
        settings.setMaxTokens(800);
        settings.setTemperature(new BigDecimal("0.2"));
        return settings;
    }
}
