package com.scut.mailsystem.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ApiKeyCryptoUtils {

    private static final byte[] API_KEY_ENCODE_SECRET = "mail-system-api-key-dev-secret".getBytes(StandardCharsets.UTF_8);

    private ApiKeyCryptoUtils() {
    }

    public static String encodeApiKey(String apiKey) {
        byte[] bytes = apiKey.getBytes(StandardCharsets.UTF_8);
        byte[] encodedBytes = xor(bytes);
        return Base64.getEncoder().encodeToString(encodedBytes);
    }

    public static String decodeApiKey(String encodedApiKey) {
        try {
            byte[] encodedBytes = Base64.getDecoder().decode(encodedApiKey);
            byte[] bytes = xor(encodedBytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static String maskApiKey(String apiKey) {
        String trimmed = apiKey.trim();
        if (trimmed.length() >= 7 && trimmed.startsWith("sk-")) {
            return "sk-****" + trimmed.substring(trimmed.length() - 4);
        }
        if (trimmed.length() >= 4) {
            return "****" + trimmed.substring(trimmed.length() - 2);
        }
        return "****";
    }

    private static byte[] xor(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (byte) (bytes[i] ^ API_KEY_ENCODE_SECRET[i % API_KEY_ENCODE_SECRET.length]);
        }
        return result;
    }
}
