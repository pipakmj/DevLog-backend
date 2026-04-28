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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitAnalyzeController {
    private final GitHubApiService gitHubApiService;
    private final TechStackExtractor techStackExtractor;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @PostMapping("/analyze")
    @Cacheable(value = "gitAnalyzeCache", key = "#request.gitUrl")
    public ResponseEntity<ApiResponse<GitAnalyzeResponse>> analyzeRepo(@RequestBody GitAnalyzeRequest request) {
        try {
            String ownerRepo = gitHubApiService.extractOwnerRepo(request.getGitUrl());

            // 3개의 GitHub API 호출을 병렬로 실행
            CompletableFuture<List<String>> rootFilesFuture = CompletableFuture.supplyAsync(
                    () -> gitHubApiService.getRootFiles(ownerRepo));
            CompletableFuture<String> readmeFuture = CompletableFuture.supplyAsync(
                    () -> gitHubApiService.getReadme(ownerRepo));
            CompletableFuture<List<String>> commitsFuture = CompletableFuture.supplyAsync(
                    () -> gitHubApiService.getRecentCommits(ownerRepo, 20));

            CompletableFuture.allOf(rootFilesFuture, readmeFuture, commitsFuture).join();

            List<String> rootFiles = rootFilesFuture.get();
            String readme = readmeFuture.get();
            List<String> commits = commitsFuture.get();

            List<String> techStack = techStackExtractor.extractTechStack(rootFiles);

            String aiResult = geminiService.summarize(readme, commits);

            GitAnalyzeResponse response = buildResponse(techStack, aiResult);
            return ResponseEntity.ok(ApiResponse.success("프로젝트 분석이 완료 되었습니다.", response));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("프로젝트 분석 중 오류가 발생했습니다.", e);
        }
    }

    private GitAnalyzeResponse buildResponse(List<String> techStack, String aiResult) {
        String description = "";
        List<String> features = new ArrayList<>();
        Set<String> finalTechStack = new HashSet<>(techStack);
        try {
            // AI가 반환한 JSON 문자열 파싱
            // Gemini가 ```json ... ``` 으로 감싸서 줄 수 있으므로 제거
            String cleaned = aiResult
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            JsonNode json = objectMapper.readTree(cleaned);
            description = json.get("description").asText();
            if (json.has("techStack")) {
                for (JsonNode tech : json.get("techStack")) {
                    finalTechStack.add(tech.asText());
                }
            }
            for (JsonNode feature : json.get("features")) {
                features.add(feature.asText());
            }
        } catch (Exception e) {
            // 파싱 실패 시 AI 원문을 description에 그대로 넣기
            description = aiResult;
        }
        return GitAnalyzeResponse.builder()
                .techStack(new ArrayList<>(finalTechStack))
                .description(description)
                .features(features)
                .build();
    }
}
