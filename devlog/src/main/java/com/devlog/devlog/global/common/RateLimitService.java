package com.devlog.devlog.global.common;

import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RateLimitService {
        private final StringRedisTemplate redisTemplate;

        private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

        public void validateLimit(
                        String prefix,
                        String userEmail,
                        int dailyLimit,
                        ErrorCode errorCode) {

                String key = prefix + userEmail;

                String value = redisTemplate.opsForValue().get(key);

                int used = value == null
                                ? 0
                                : Integer.parseInt(value);

                if (used >= dailyLimit) {

                        throw new BusinessException(
                                        errorCode);
                }
        }

        public UsageLimitResponse consume(
                        String prefix,
                        String userEmail,
                        int dailyLimit) {

                String key = prefix + userEmail;

                Long count = redisTemplate.opsForValue()
                                .increment(key);

                if (count != null && count == 1L) {
                        redisTemplate.expire(
                                        key,
                                        untilMidnight());
                }

                return UsageLimitResponse.builder()
                                .dailyLimit(dailyLimit)
                                .used(count.intValue())
                                .remaining(
                                                dailyLimit - count.intValue())
                                .resetAt(nextMidnight())
                                .build();
        }

        public Duration untilMidnight() {

                ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

                ZonedDateTime midnight = now.plusDays(1)
                                .toLocalDate()
                                .atStartOfDay(ZONE_ID);

                return Duration.between(
                                now,
                                midnight);
        }

        public String nextMidnight() {

                ZonedDateTime midnight = ZonedDateTime.now(ZONE_ID)
                                .plusDays(1)
                                .toLocalDate()
                                .atStartOfDay(ZONE_ID);

                return midnight.format(
                                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

}
