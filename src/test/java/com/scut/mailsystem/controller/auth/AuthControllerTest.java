package com.scut.mailsystem.controller.auth;

import com.scut.mailsystem.dto.auth.LoginRequest;
import com.scut.mailsystem.dto.auth.RegisterRequest;
import com.scut.mailsystem.service.auth.AuthService;
import com.scut.mailsystem.vo.auth.LoginUserVO;
import com.scut.mailsystem.vo.auth.LoginVO;
import com.scut.mailsystem.vo.auth.RegisterVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(authService))
            .build();

    @Test
    void register_passesJsonBodyToServiceAndReturnsFinalContractShape() throws Exception {
        when(authService.register(org.mockito.ArgumentMatchers.any(RegisterRequest.class)))
                .thenReturn(new RegisterVO("zhangsan", "张三"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "zhangsan",
                                  "password": "123456",
                                  "nickname": "张三"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.nickname").value("张三"));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertEquals("zhangsan", captor.getValue().getUsername());
        assertEquals("123456", captor.getValue().getPassword());
        assertEquals("张三", captor.getValue().getNickname());
    }

    @Test
    void login_passesJsonBodyToServiceAndReturnsTokenWithUser() throws Exception {
        when(authService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class)))
                .thenReturn(new LoginVO(
                        "mock-token-admin-123456",
                        new LoginUserVO("admin", "管理员", "admin@mail.com")
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.token").value("mock-token-admin-123456"))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.nickname").value("管理员"))
                .andExpect(jsonPath("$.data.user.emailAddress").value("admin@mail.com"));

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("123456", captor.getValue().getPassword());
    }

    @Test
    void logout_passesAuthorizationHeaderToService() throws Exception {
        when(authService.logout("Bearer token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value(true));

        verify(authService).logout("Bearer token");
    }
}
