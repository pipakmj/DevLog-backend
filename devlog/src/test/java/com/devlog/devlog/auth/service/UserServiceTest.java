package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.UserRole;
import com.devlog.devlog.auth.dto.auth.SignInRequest;
import com.devlog.devlog.auth.dto.auth.SignUpRequest;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.RefreshTokenRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.ValificationCodeGenerator;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ValificationCodeGenerator valificationCodeGenerator;

    @Mock
    private MailService mailService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Nested
    @DisplayName("회원가입 기능 테스트")
    class SignUpTest {

        @Test
        @DisplayName("성공: 필수 조건과 미중복 이메일, 이메일 인증 통과 시 회원가입이 완료된다.")
        void signUp_Success() {
            // given
            SignUpRequest request = new SignUpRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123!");
            request.setNickname("테스터");
            request.setBio("안녕하세요");
            request.setGithub_url("https://github.com/test");

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("test@example.com_VERIFIED")).thenReturn("true");
            when(passwordEncoder.encode("password123!")).thenReturn("encodedPassword");

            // when
            userService.signUp(request);

            // then
            verify(userRepository, times(1)).save(any(UserEntity.class));
            verify(redisTemplate, times(1)).delete("test@example.com_VERIFIED");
        }

        @Test
        @DisplayName("실패: 이메일이 중복되는 경우 BusinessException(DUPLICATE_EMAIL)이 발생한다.")
        void signUp_Fail_DuplicateEmail() {
            // given
            SignUpRequest request = new SignUpRequest();
            request.setEmail("test@example.com");

            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

            verify(userRepository, never()).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("실패: 이메일 인증이 완료되지 않은 경우 RuntimeException이 발생한다.")
        void signUp_Fail_NotVerified() {
            // given
            SignUpRequest request = new SignUpRequest();
            request.setEmail("test@example.com");

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("test@example.com_VERIFIED")).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이메일 인증이 완료되지 않았습니다.");

            verify(userRepository, never()).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("로그인 기능 테스트")
    class SignInTest {

        @Test
        @DisplayName("성공: 올바른 계정 정보 입력 시 유저 정보를 반환한다.")
        void signIn_Success() {
            // given
            SignInRequest request = new SignInRequest();
            ReflectionTestUtils.setField(request, "email", "test@example.com");
            ReflectionTestUtils.setField(request, "password", "password123!");

            UserEntity user = UserEntity.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .role(UserRole.ROLE_USER)
                    .build();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123!", "encodedPassword")).thenReturn(true);

            // when
            UserEntity result = userService.signIn(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("실패: 가입되지 않은 이메일의 경우 USER_NOT_FOUND 예외가 발생한다.")
        void signIn_Fail_UserNotFound() {
            // given
            SignInRequest request = new SignInRequest();
            ReflectionTestUtils.setField(request, "email", "wrong@example.com");
            ReflectionTestUtils.setField(request, "password", "password123!");

            when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.signIn(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 패스워드가 잘못된 경우 INVALID_PASSWORD 예외가 발생한다.")
        void signIn_Fail_InvalidPassword() {
            // given
            SignInRequest request = new SignInRequest();
            ReflectionTestUtils.setField(request, "email", "test@example.com");
            ReflectionTestUtils.setField(request, "password", "wrongpassword");

            UserEntity user = UserEntity.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .build();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.signIn(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
        }
    }
}
