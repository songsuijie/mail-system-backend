package com.scut.mailsystem.service.impl;

import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.entity.SysUser;
import com.scut.mailsystem.exception.BusinessException;
import com.scut.mailsystem.mapper.SysUserMapper;
import com.scut.mailsystem.service.user.UserService;
import com.scut.mailsystem.utils.AuthHeaderUtils;
import com.scut.mailsystem.utils.TokenUtils;
import com.scut.mailsystem.vo.user.CurrentUserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

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

    private String getAvatarText(SysUser user) {
        String source = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        if (!StringUtils.hasText(source)) {
            return "";
        }
        return source.substring(0, 1);
    }
}
