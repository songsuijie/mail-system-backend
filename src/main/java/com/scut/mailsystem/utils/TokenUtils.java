package com.scut.mailsystem.utils;

import com.scut.mailsystem.common.LoginUser;
import com.scut.mailsystem.common.enums.ErrorCode;
import com.scut.mailsystem.exception.BusinessException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

public final class TokenUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long DEFAULT_EXPIRE_MILLIS = 7L * 24 * 60 * 60 * 1000;

    // Can be moved to application.yml later.
    private static final String SECRET = "mail-system-dev-secret-change-me";

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private TokenUtils() {
    }

    public static String generateToken(Long userId, String username) {
        long expireAt = System.currentTimeMillis() + DEFAULT_EXPIRE_MILLIS;
        String payload = userId + ":" + username + ":" + expireAt;
        String payloadSegment = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        String signatureSegment = base64UrlEncode(sign(payloadSegment));
        return payloadSegment + "." + signatureSegment;
    }

    public static LoginUser parseToken(String token) {
        if (token == null || token.isBlank()) {
            throwUnauthorized();
        }

        String[] tokenParts = token.split("\\.", -1);
        if (tokenParts.length != 2 || tokenParts[0].isBlank() || tokenParts[1].isBlank()) {
            throwUnauthorized();
        }

        String payloadSegment = tokenParts[0];
        String signatureSegment = tokenParts[1];
        if (!matchesSignature(payloadSegment, signatureSegment)) {
            throwUnauthorized();
        }

        String payload = decodePayload(payloadSegment);
        String[] payloadParts = payload.split(":", -1);
        if (payloadParts.length != 3 || payloadParts[0].isBlank()
                || payloadParts[1].isBlank() || payloadParts[2].isBlank()) {
            throwUnauthorized();
        }

        try {
            Long userId = Long.parseLong(payloadParts[0]);
            String username = payloadParts[1];
            Long expireAt = Long.parseLong(payloadParts[2]);
            if (expireAt <= System.currentTimeMillis()) {
                throwUnauthorized();
            }
            return new LoginUser(userId, username, expireAt);
        } catch (NumberFormatException exception) {
            throwUnauthorized();
            return null;
        }
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    private static String decodePayload(String payloadSegment) {
        try {
            byte[] payloadBytes = BASE64_URL_DECODER.decode(payloadSegment);
            return new String(payloadBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throwUnauthorized();
            return null;
        }
    }

    private static boolean matchesSignature(String payloadSegment, String signatureSegment) {
        String expectedSignature = base64UrlEncode(sign(payloadSegment));
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signatureSegment.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static byte[] sign(String payloadSegment) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            return mac.doFinal(payloadSegment.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to sign token", exception);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }

    private static void throwUnauthorized() {
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
}
