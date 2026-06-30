package com.devlog.devlog.global.common;

import com.devlog.devlog.global.exception.AiFeedbackLimitException;
import com.devlog.devlog.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimtService 단위테스트")
public class RateLimitServiceTest {
    @InjectMocks
    private RateLimitService rateLimitService;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("실패: 일일제한치(5회)를 초과하여 요청 시 AiFeedbackLimitException이 발생하고 롤백 메소드가 실행된다.")
    void checkAndIncrement_Fail_LimitExceeded() {
        String prefix = "portfolio_limit";
        String email = "test@test.com";
        int limit = 5;

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(prefix + email)).thenReturn(6L);

        assertThatThrownBy(() -> rateLimitService.checkAndIncrement(
                prefix,email,limit, ErrorCode.AI_FEEDBACK_DAILY_LIMIT_EXCEEDED
        )).isInstanceOf(AiFeedbackLimitException.class);

        verify(valueOperations, times(1)).decrement(prefix + email);
    }

    @Test
    @DisplayName("성공: 일일제한치(5회) 이내 정상 요청 시 남은 횟수를 포함한 성공 응답이 반환된다.")
    void checkAndIncrement_Success_UnderLimit() {
        String prefix = "portfolio_limit";
        String email = "test@test.com";
        int limit = 5;

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(prefix + email)).thenReturn(3L);

        UsageLimitResponse response = rateLimitService.checkAndIncrement(
                prefix, email, limit, ErrorCode.AI_FEEDBACK_DAILY_LIMIT_EXCEEDED
        );

        assertThat(response.getDailyLimit()).isEqualTo(5);
        assertThat(response.getUsed()).isEqualTo(3);
        assertThat(response.getRemaining()).isEqualTo(2);

        verify(valueOperations, never()).decrement(anyString());
    }
}
