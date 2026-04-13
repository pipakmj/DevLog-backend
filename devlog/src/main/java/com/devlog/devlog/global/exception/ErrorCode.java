package com.devlog.devlog.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-001", "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH-002", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH-003", "이미 존재하는 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-004", "잘못된 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-005", "만료된 토큰입니다."),

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT-001", "프로젝트를 찾을 수 없습니다."),
    UNAUTHORIZED_PROJECT_ACCESS(HttpStatus.FORBIDDEN, "PROJECT-002", "프로젝트에 대한 권한이 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-001", "포스트를 찾을 수 없습니다."),
    UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "POST-002", "포스트에 대한 권한이 없습니다."),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
