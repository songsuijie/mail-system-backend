package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.dto.user.ChangePasswordRequest;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.user.UserService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.PasswordUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.user.CurrentUserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final SysUserMapper sysUserMapper;

    public UserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public CurrentUserVO getCurrentUser(String authorizationHeader) {
        String token = AuthHeaderUtils.extractToken(authorizationHeader);
        LoginUser loginUser = TokenUtils.parseToken(token);
        SysUser user = sysUserMapper.selectActiveById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return new CurrentUserVO(
                user.getUsername(),
                user.getNickname(),
                user.getEmailAddress(),
                getAvatarText(user)
        );
    }

    @Override
    @Transactional
    public void changePassword(String authorizationHeader, ChangePasswordRequest request) {
        String token = AuthHeaderUtils.extractToken(authorizationHeader);
        LoginUser loginUser = TokenUtils.parseToken(token);
        SysUser user = sysUserMapper.selectActiveById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        validateChangePasswordRequest(request, user);
        int updated = sysUserMapper.updatePasswordHash(PasswordUtils.sha256(request.getNewPassword()), user.getId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String getAvatarText(SysUser user) {
        String source = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        if (!StringUtils.hasText(source)) {
            return "";
        }
        return source.substring(0, 1);
    }

    private void validateChangePasswordRequest(ChangePasswordRequest request, SysUser user) {
        if (request == null || !StringUtils.hasText(request.getOldPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "原密码不能为空");
        }
        if (!PasswordUtils.sha256(request.getOldPassword()).equals(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "原密码错误");
        }
        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能为空");
        }
        if (!StringUtils.hasText(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "确认密码不能为空");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的新密码不一致");
        }
        if (request.getNewPassword().length() < MIN_PASSWORD_LENGTH
                || request.getNewPassword().length() > MAX_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码长度不合法");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能与原密码相同");
        }
    }
}
