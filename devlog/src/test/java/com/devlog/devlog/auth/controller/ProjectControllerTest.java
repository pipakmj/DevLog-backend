package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.project.ProjectRequest;
import com.devlog.devlog.auth.dto.project.ProjectResponse;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.service.ProjectService;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.devlog.devlog.global.config.SecurityConfig;

@WebMvcTest(controllers = ProjectController.class)
@Import(SecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("ProjectController 슬라이스 테스트")
class ProjectControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private ProjectService projectService;

        @MockitoBean
        private ProjectRepository projectRepository;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @Test
        @DisplayName("성공: 비로그인 상태에서 프로젝트 개별 상세 조회가 가능하다.")
        void getDetailProject_Success() throws Exception {
                // given
                Long projectId = 1L;
                ProjectResponse response = ProjectResponse.builder()
                                .title("테스트 프로젝트")
                                .description("설명")
                                .demoUrl("https://demo.com")
                                .githubUrl("https://github.com/demo")
                                .techStack("Java, Spring")
                                .build();

                when(projectService.getDetailProject(projectId)).thenReturn(response);

                // when & then
                mockMvc.perform(get("/api/project/{id}", projectId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.title").value("테스트 프로젝트"))
                                .andExpect(jsonPath("$.data.techStack").value("Java, Spring"));
        }

        @Test
        @WithMockUser(username = "user@example.com")
        @DisplayName("성공: 로그인 상태에서 프로젝트 생성이 가능하다.")
        void createProject_Success() throws Exception {
                // given
                ProjectRequest request = ProjectRequest.builder()
                                .title("새로운 프로젝트")
                                .description("새 프로젝트 설명")
                                .techStack("Kotlin, Spring")
                                .build();

                ProjectResponse response = ProjectResponse.builder()
                                .title("새로운 프로젝트")
                                .description("새 프로젝트 설명")
                                .techStack("Kotlin, Spring")
                                .build();

                when(projectService.createProject(eq("user@example.com"), any(ProjectRequest.class)))
                                .thenReturn(response);

                // when & then
                mockMvc.perform(post("/api/project/create")
                                .with(csrf()) // Spring Security가 활성화되어 있는 상태에서 CSRF 처리 대응
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.title").value("새로운 프로젝트"));
        }
}
