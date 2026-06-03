package com.scut.mailsystem.controller.auth;

import com.scut.mailsystem.common.ApiResponse;
import com.scut.mailsystem.dto.auth.LoginRequest;
import com.scut.mailsystem.dto.auth.RegisterRequest;
import com.scut.mailsystem.service.auth.AuthService;
import com.scut.mailsystem.vo.auth.LoginVO;
import com.scut.mailsystem.vo.auth.RegisterVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@RequestHeader(value = "Authorization", required = false)
                                       String authorizationHeader) {
        return ApiResponse.success(authService.logout(authorizationHeader));
    }
}
