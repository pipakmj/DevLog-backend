package com.devlog.devlog.global.common;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ValificationCodeGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
