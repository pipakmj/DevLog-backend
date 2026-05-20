package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.github.GitAnalyzeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GitAnalyzeService {
    private final GitHubApiService gitHubApiService;
    private final TechStackExtractor techStackExtractor;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final Executor httpExecutor = Executors.newFixedThreadPool(10);

    @Cacheable(value = "gitAnalyzeCache", key = "#gitUrl")
    public GitAnalyzeResponse analyze(String gitUrl) {
        try {
            String ownerRepo = gitHubApiService.extractOwnerRepo(gitUrl);

            CompletableFuture<List<String>> rootFilesFuture = CompletableFuture.supplyAsync(
                    () -> gitHubApiService.getRootFiles(ownerRepo), httpExecutor);
            CompletableFuture<String> readmeFuture = CompletableFuture.supplyAsync(
                    () -> gitHubApiService.getReadme(ownerRepo), httpExecutor);
            CompletableFuture<List<String>> commitsFuture = CompletableFuture.supplyAsync(
                    () -> gitHubApiService.getRecentCommits(ownerRepo, 20), httpExecutor);

            CompletableFuture.allOf(rootFilesFuture, readmeFuture, commitsFuture).join();

            List<String> rootFiles = rootFilesFuture.get();
            String readme = readmeFuture.get();
            List<String> commits = commitsFuture.get();

            List<String> techStack = techStackExtractor.extractTechStack(rootFiles);

            String aiResult = geminiService.summarize(readme, commits);

            return buildResponse(techStack, aiResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("분석 중 오류가 발생했습니다.", e);
        }
    }

    private GitAnalyzeResponse buildResponse(List<String> techStack, String aiResult) {
        String description = "";
        List<String> features = new ArrayList<>();
        Set<String> finalTechStack = new HashSet<>(techStack);
        try {
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
            description = aiResult;
        }
        return GitAnalyzeResponse.builder()
                .techStack(new ArrayList<>(finalTechStack))
                .description(description)
                .features(features)
                .build();
    }
}
