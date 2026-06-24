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
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT-001", "댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "COMMENT-002", "댓글에 대한 권한이 없습니다."),
    API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "API-001", "일일 요청 제한에 도달했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "PROJECT-003", "허용되지 않은 주소입니다."),

    //Portfolio
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "PORTFOLIO-001", "포트폴리오를 찾을 수 없습니다."),
    UNAUTHORIZED_PORTFOLIO_ACCESS(HttpStatus.FORBIDDEN, "PORTFOLIO-002", "포트폴리오에 대한 권한이 없습니다."),
    AI_FEEDBACK_DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS,"PORTFOLIO-003","오늘 AI 개선 사용량을 모두 사용했습니다. 내일 다시 시도해 주세요."),

    // Bookmark
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKMARK-001", "북마크를 찾을 수 없습니다."),
    UNAUTHORIZED_BOOKMARK_ACCESS(HttpStatus.FORBIDDEN, "BOOKMARK-002", "북마크에 대한 권한이 없습니다."),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
