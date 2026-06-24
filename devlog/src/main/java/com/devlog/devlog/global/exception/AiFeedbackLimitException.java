package com.devlog.devlog.global.exception;

import com.devlog.devlog.global.common.UsageLimitResponse;
import lombok.Getter;

@Getter
public class AiFeedbackLimitException extends RuntimeException {
    private final UsageLimitResponse usageLimitResponse;
    public AiFeedbackLimitException(UsageLimitResponse usageLimitResponse) {
        super("오늘 AI 개선 사용량을 모두 사용했습니다. 내일 다시 도전해주세요. ");
        this.usageLimitResponse = usageLimitResponse;
    }
}
