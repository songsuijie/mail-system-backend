package com.scut.mailsystem.utils;

import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.exception.BusinessException;

public final class AuthHeaderUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private AuthHeaderUtils() {
    }

    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return token;
    }
}
