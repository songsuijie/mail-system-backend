package com.scut.mailsystem.service.user;

import com.scut.mailsystem.dto.user.ChangePasswordRequest;
import com.scut.mailsystem.vo.user.CurrentUserVO;

public interface UserService {

    CurrentUserVO getCurrentUser(String authorizationHeader);

    void changePassword(String authorizationHeader, ChangePasswordRequest request);
}
