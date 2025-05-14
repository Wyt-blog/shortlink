package com.nageoffer.shortlink.admin.toolkit;

import java.security.SecureRandom;

public class RandomCodeGenerator {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须为正整数");
        }

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }

    public static String generateRandomCode() {
        return generateRandomCode(6);
    }

}
