package com.devlog.devlog.global.exception;

import com.devlog.devlog.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        protected ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException e) {
                String errorMessage = e.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(errorMessage, "VALIDATION-001"));
        }

        @ExceptionHandler(BusinessException.class)
        protected ResponseEntity<ApiResponse<String>> handleBusinessException(BusinessException e) {
                ErrorCode errorCode = e.getErrorCode();
                return ResponseEntity.status(errorCode.getHttpStatus())
                                .body(ApiResponse.error(errorCode.getMessage(), errorCode.getCode()));
        }

        @ExceptionHandler(Exception.class)
        protected ResponseEntity<ApiResponse<String>> handleException(Exception e) {
                // 서버 콘솔에 실제 에러의 원인을 출력해주는 코드 추가!
                e.printStackTrace();

                return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                                                ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
        }
}
