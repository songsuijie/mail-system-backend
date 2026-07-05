package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.user.ChangePasswordRequest;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.utils.PasswordUtils;
import com.scut.mailsystem.utils.TokenUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class UserServiceImplTest {

    private final SysUserMapper sysUserMapper = mock(SysUserMapper.class);
    private final UserServiceImpl userService = new UserServiceImpl(sysUserMapper);

    @Test
    void changePassword_withValidOldPasswordUpdatesPasswordHash() {
        SysUser alice = activeUser(1L, "alice", "123456");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);
        when(sysUserMapper.updatePasswordHash(anyString(), eq(1L))).thenReturn(1);

        ChangePasswordRequest request = changePasswordRequest("123456", "new123456", "new123456");

        userService.changePassword(authHeader(1L, "alice"), request);

        ArgumentCaptor<String> passwordHashCaptor = ArgumentCaptor.forClass(String.class);
        verify(sysUserMapper).updatePasswordHash(passwordHashCaptor.capture(), eq(1L));
        assertEquals(PasswordUtils.sha256("new123456"), passwordHashCaptor.getValue());
        assertNotEquals(alice.getPasswordHash(), passwordHashCaptor.getValue());
    }

    @Test
    void changePassword_withWrongOldPasswordReturnsParamError() {
        SysUser alice = activeUser(1L, "alice", "123456");
        when(sysUserMapper.selectActiveById(1L)).thenReturn(alice);

        ChangePasswordRequest request = changePasswordRequest("bad-password", "new123456", "new123456");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.changePassword(authHeader(1L, "alice"), request)
        );

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertEquals("原密码错误", exception.getMessage());
    }

    private ChangePasswordRequest changePasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(oldPassword);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private SysUser activeUser(Long id, String username, String password) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        user.setPasswordHash(PasswordUtils.sha256(password));
        return user;
    }

    private String authHeader(Long userId, String username) {
        return "Bearer " + TokenUtils.generateToken(userId, username);
    }
}
