package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.request.AiFeedbackRequest;
import com.devlog.devlog.auth.dto.portfolio.request.CreatePortfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.request.SharePortfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.response.SharePortfolioResponse;
import com.devlog.devlog.auth.entity.PortfolioEntity;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.PortfolioRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.PdfGenerator;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService 단위 테스트")
public class PortfolioServiceTest {
        @InjectMocks
        private PortfolioService portfolioService;
        @Mock
        private UserRepository userRepository;
        @Mock
        private ProjectRepository projectRepository;
        @Mock
        private PortfolioRepository portfolioRepository;
        @Mock
        private PdfGenerator pdfGenerator;
        @Mock
        private GeminiService geminiService;
        @Spy
        private ObjectMapper objectMapper = new ObjectMapper();

        @Nested
        @DisplayName("포트폴리오 생성 테스트")
        class CreatePortfolioTest {
                @Test
                @DisplayName("실패: 본인의 프로젝트가 아닌 타인의 프로젝트 ID로 생성 요청 시 UNAUTHORIZED_PROJECT_ACCESS 에러가 발생한다.")
                void createPortfolio_Fail_UnauthorizedProject() {
                        String userEmail = "test@test.com";
                        UserEntity me = UserEntity.builder().email(userEmail).build();
                        UserEntity otherUser = UserEntity.builder().email("other@test.com").build();

                        ProjectEntity otherProject = ProjectEntity.builder()
                                        .title("남의 프로젝트")
                                        .userEntity(otherUser)
                                        .build();
                        CreatePortfolioRequest request = CreatePortfolioRequest.builder()
                                        .projectId(10L)
                                        .status("DRAFT")
                                        .build();
                        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(me));
                        when(projectRepository.findById(10L)).thenReturn(Optional.of(otherProject));

                        assertThatThrownBy(() -> portfolioService.createPortfolio(userEmail, request))
                                        .isInstanceOf(BusinessException.class)
                                        .extracting("errorCode")
                                        .isEqualTo(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);

                        verify(portfolioRepository, never()).save(any());
                }

                @Test
                @DisplayName("성공: 본인의 프로젝트로 포트폴리오를 생성 시 필수 필드가 채워진 정상 요청은 성공적으로 저장된다.")
                void createPortfolio_Success() throws Exception {
                        String userEmail = "test@test.com";
                        UserEntity me = UserEntity.builder().email(userEmail).build();
                        ReflectionTestUtils.setField(me, "id", 1);
                        ProjectEntity myProject = ProjectEntity.builder()
                                        .title("내 프로젝트")
                                        .userEntity(me)
                                        .build();

                        CreatePortfolioRequest request = CreatePortfolioRequest.builder()
                                        .projectId(10L)
                                        .overview("내 포트폴리오 개요입니다")
                                        .roles("백엔드 역할")
                                        .techStack(java.util.List.of("Java", "Spring"))
                                        .features(
                                                        java.util.List.of(
                                                                        new com.devlog.devlog.auth.dto.portfolio.FeatureDTO(
                                                                                        "로그인", "로그인 기능 구현")))
                                        .status("COMPLETED")
                                        .build();

                        PortfolioEntity pretendSavedPortfolio = PortfolioEntity.builder()
                                        .project(myProject)
                                        .status("COMPLETED")
                                        .build();
                        ReflectionTestUtils.setField(pretendSavedPortfolio, "id", 100L);

                        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(me));
                        when(projectRepository.findById(10L)).thenReturn(Optional.of(myProject));
                        when(portfolioRepository.save(any(PortfolioEntity.class))).thenReturn(pretendSavedPortfolio);

                        com.devlog.devlog.auth.dto.portfolio.response.PortfolioResponse response = portfolioService
                                        .createPortfolio(userEmail, request);

                        assertThat(response.getPortfolioId()).isEqualTo(100L);
                        assertThat(response.getStatus()).isEqualTo("COMPLETED");

                        verify(portfolioRepository, times(1)).save(any(PortfolioEntity.class));
                }
        }

        @Nested
        @DisplayName("포트폴리오 공유 여부 변경 테스트")
        class SharePortfolioTest {
                @Test
                @DisplayName("성공: 포트폴리오를 공개(public)로 설정하면 8자리 코드값의 shareToken이 정상 생성된다.")
                void sharePortfolio_Success_ToPublic() {
                        String userEmail = "test@test.com";
                        UserEntity user = UserEntity.builder().email(userEmail).build();
                        ReflectionTestUtils.setField(user, "id", 1);

                        ProjectEntity myProject = ProjectEntity.builder().userEntity(user).build();

                        PortfolioEntity portfolio = PortfolioEntity.builder()
                                        .project(myProject)
                                        .isPublic(false)
                                        .shareToken(null)
                                        .build();
                        ReflectionTestUtils.setField(portfolio, "id", 10L);

                        SharePortfolioRequest request = SharePortfolioRequest.builder().isPublic(true).build();

                        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
                        when(portfolioRepository.findById(10L)).thenReturn(Optional.of(portfolio));

                        SharePortfolioResponse response = portfolioService.sharePortfolio(userEmail, 10L, request);

                        assertThat(response.isPublic()).isTrue();
                        assertThat(response.getShareToken()).isNotNull();
                        assertThat(response.getShareUrl()).contains(response.getShareToken());

                        verify(portfolioRepository, times(1)).save(portfolio);
                }
        }

        @Nested
        @DisplayName("포트폴리오 삭제 권한 테스트")
        class DeletePortfolioTest {
                @Test
                @DisplayName("실패: 타인의 포트폴리오를 삭제하려 하면 UNAUTHORIZED_PORTFOLIO_ACCESS 예외가 발생한다")
                void deletePortfolio_Fail_UnauthorizedPortfolio() {
                        String userEmail = "test@test.com";
                        UserEntity me = UserEntity.builder().email(userEmail).build();
                        ReflectionTestUtils.setField(me, "id", 1);

                        UserEntity otherUser = UserEntity.builder().email("other@test.com").build();
                        ReflectionTestUtils.setField(otherUser, "id", 99);

                        ProjectEntity otherProject = ProjectEntity.builder().userEntity(otherUser).build();
                        PortfolioEntity otherPortfolio = PortfolioEntity.builder().project(otherProject).build();

                        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(me));
                        when(portfolioRepository.findById(10L)).thenReturn(Optional.of(otherPortfolio));

                        assertThatThrownBy(() -> portfolioService.deletePortfolio(userEmail, 10L))
                                        .isInstanceOf(BusinessException.class)
                                        .extracting("errorCode")
                                        .isEqualTo(ErrorCode.UNAUTHORIZED_PORTFOLIO_ACCESS);

                        verify(portfolioRepository, never()).delete(any());
                }
        }

        @Nested
        @DisplayName("AI 피드백 생성 테스트")
        class CreateAiFeedbackTest {
                @Test
                @DisplayName("실패: 타인의 프로젝트로 AI 피드백을 요청하면 UNAUTHORIZED_PROJECT_ACCESS가 발생한다")
                void createAiFeedback_Fail_UnauthorizedProject() {
                        String userEmail = "test@test.com";
                        UserEntity me = UserEntity.builder().email(userEmail).build();
                        UserEntity otherUser = UserEntity.builder().email("other@test.com").build();
                        ProjectEntity otherProject = ProjectEntity.builder().userEntity(otherUser).build();

                        AiFeedbackRequest request = AiFeedbackRequest.builder()
                                        .projectId(1L)
                                        .build();

                        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(me));
                        when(projectRepository.findById(1L)).thenReturn(Optional.of(otherProject));

                        assertThatThrownBy(() -> portfolioService.createAiFeedback(userEmail, request))
                                        .isInstanceOf(BusinessException.class)
                                        .extracting("errorCode")
                                        .isEqualTo(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);

                        verify(geminiService, never()).PortfolioAiFeedback(any());
                }

                @Test
                @DisplayName("Gemini API 장애 시 RuntimeException이 호출자에게 정상 전파된다.")
                void createAiFeedback_Fail_GeminiError() {
                        String userEmail = "test@test.com";
                        UserEntity me = UserEntity.builder().email(userEmail).build();
                        ProjectEntity myProject = ProjectEntity.builder().userEntity(me).build();

                        AiFeedbackRequest request = AiFeedbackRequest.builder()
                                        .projectId(1L)
                                        .build();

                        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(me));
                        when(projectRepository.findById(1L)).thenReturn(Optional.of(myProject));
                        when(geminiService.PortfolioAiFeedback(any())).thenThrow(new RuntimeException("Gemini Error"));

                        assertThatThrownBy(() -> portfolioService.createAiFeedback(userEmail, request))
                                        .isInstanceOf(RuntimeException.class)
                                        .hasMessageContaining("Gemini Error");
                }
        }
}
