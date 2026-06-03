package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.auth.LoginRequest;
import com.scut.mailsystem.dto.auth.RegisterRequest;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.auth.AuthService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.PasswordUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.auth.LoginUserVO;
import com.scut.mailsystem.vo.auth.LoginVO;
import com.scut.mailsystem.vo.auth.RegisterVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int USER_STATUS_NORMAL = 1;
    private static final int NOT_DELETED = 0;

    private final SysUserMapper sysUserMapper;

    public AuthServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional
    public RegisterVO register(RegisterRequest request) {
        String username = trim(request.getUsername());
        String password = request.getPassword();
        String nickname = trim(request.getNickname());
        if (!StringUtils.hasText(nickname)) {
            nickname = username;
        }

        if (sysUserMapper.selectByUsername(username) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtils.sha256(password));
        user.setNickname(nickname);
        user.setEmailAddress(username + "@mail.com");
        user.setStatus(USER_STATUS_NORMAL);
        user.setDeleted(NOT_DELETED);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        return new RegisterVO(user.getUsername(), user.getNickname());
    }

    @Override
    public LoginVO login(LoginRequest request) {
        String username = trim(request.getUsername());
        String passwordHash = PasswordUtils.sha256(request.getPassword());
        SysUser user = sysUserMapper.selectActiveByUsername(username);
        if (user == null || !passwordHash.equals(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        String token = TokenUtils.generateToken(user.getId(), user.getUsername());
        LoginUserVO userVO = new LoginUserVO(user.getUsername(), user.getNickname(), user.getEmailAddress());
        return new LoginVO(token, userVO);
    }

    @Override
    public Boolean logout(String authorizationHeader) {
        String token = AuthHeaderUtils.extractToken(authorizationHeader);
        TokenUtils.parseToken(token);
        return true;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
