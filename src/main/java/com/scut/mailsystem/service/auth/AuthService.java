package com.scut.mailsystem.service.auth;

import com.scut.mailsystem.dto.auth.LoginRequest;
import com.scut.mailsystem.dto.auth.RegisterRequest;
import com.scut.mailsystem.vo.auth.LoginVO;
import com.scut.mailsystem.vo.auth.RegisterVO;

public interface AuthService {

    RegisterVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    Boolean logout(String authorizationHeader);
}
