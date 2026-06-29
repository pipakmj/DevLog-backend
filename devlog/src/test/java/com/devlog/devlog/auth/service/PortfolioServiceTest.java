package com.devlog.devlog.auth.service;

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
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);

            verify(portfolioRepository, never()).save(any());
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
            ReflectionTestUtils.setField(user,"id", 1);

            ProjectEntity myProject = ProjectEntity.builder().userEntity(user).build();

            PortfolioEntity portfolio = PortfolioEntity.builder()
                    .project(myProject)
                    .isPublic(false)
                    .shareToken(null)
                    .build();
            ReflectionTestUtils.setField(portfolio,"id",10L);

            SharePortfolioRequest request = SharePortfolioRequest.builder().isPublic(true).build();

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
            when(portfolioRepository.findById(10L)).thenReturn(Optional.of(portfolio));

            SharePortfolioResponse response = portfolioService.sharePortfolio(userEmail,10L, request);

            assertThat(response.isPublic()).isTrue();
            assertThat(response.getShareToken()).isNotNull();
            assertThat(response.getShareUrl()).contains(response.getShareToken());

            verify(portfolioRepository,times(1)).save(portfolio);
        }
    }
}
