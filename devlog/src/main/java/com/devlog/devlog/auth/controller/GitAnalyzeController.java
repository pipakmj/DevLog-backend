package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.github.GitAnalyzeRequest;
import com.devlog.devlog.auth.dto.github.GitAnalyzeResponse;
import com.devlog.devlog.auth.service.GeminiService;
import com.devlog.devlog.auth.service.GitHubApiService;
import com.devlog.devlog.auth.service.TechStackExtractor;
import com.devlog.devlog.global.common.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitAnalyzeController {
    private final GitHubApiService gitHubApiService;
    private final TechStackExtractor techStackExtractor;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<GitAnalyzeResponse>> analyzeRepo(@RequestBody GitAnalyzeRequest request) {
        try {
            String ownerRepo = gitHubApiService.extractOwnerRepo(request.getGitUrl());

            List<String> rootFiles = gitHubApiService.getRootFiles(ownerRepo);
            List<String> techStack = techStackExtractor.extractTechStack(rootFiles);

            String readme = gitHubApiService.getReadme(ownerRepo);
            List<String> commits = gitHubApiService.getRecentCommits(ownerRepo, 100);

            String aiResult = geminiService.summarize(readme, commits);

            GitAnalyzeResponse response = buildResponse(techStack, aiResult);
            return ResponseEntity.ok(ApiResponse.success("프로젝트 분석이 완료 되었습니다.", response));
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 실제 에러 로그를 출력합니다.
            throw e;
        }
    }

    private GitAnalyzeResponse buildResponse(List<String> techStack, String aiResult) {
        String description = "";
        List<String> features = new ArrayList<>();
        try {
            // AI가 반환한 JSON 문자열 파싱
            // Gemini가 ```json ... ``` 으로 감싸서 줄 수 있으므로 제거
            String cleaned = aiResult
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            JsonNode json = objectMapper.readTree(cleaned);
            description = json.get("description").asText();
            for (JsonNode feature : json.get("features")) {
                features.add(feature.asText());
            }
        } catch (Exception e) {
            // 파싱 실패 시 AI 원문을 description에 그대로 넣기
            description = aiResult;
        }
        return GitAnalyzeResponse.builder()
                .techStack(techStack)
                .description(description)
                .features(features)
                .build();
    }
}
