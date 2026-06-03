package com.scut.mailsystem.service.user;

import com.scut.mailsystem.vo.user.CurrentUserVO;

public interface UserService {

    CurrentUserVO getCurrentUser(String authorizationHeader);
}
