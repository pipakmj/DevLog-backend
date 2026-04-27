package com.devlog.devlog.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Service
@RequiredArgsConstructor
public class GitHubApiService {

    private final RestTemplate restTemplate;
    private static final String GITHUB_API = "https://api.github.com";

    public String extractOwnerRepo(String githubUrl) {
        String path = githubUrl
                .replace("https://github.com/", "")
                .replace("http://github.com/", "");
        if(path.endsWith("/")) path = path.substring(0, path.length()-1);
        if(path.endsWith(".git")) path = path.substring(0, path.length()-4);
        return path;
    }

    private HttpHeaders createHeaders() {
        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "DevLog-App");
        return headers;
    }

    // 루트 디렉토리 파일명 목록 가져오기
    public List<String> getRootFiles(String ownerRepo) {
        String url = GITHUB_API + "/repos/" + ownerRepo + "/contents/";
        ResponseEntity<JsonNode[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                JsonNode[].class
        );
        List<String> filesNames = new ArrayList<>();
        if(response.getBody() != null) {
            for(JsonNode jsonNode : response.getBody()) {
                filesNames.add(jsonNode.get("name").asText());
            }
        }
        return filesNames;
    }

    //README.md 내용 가져오기
    public String getReadme(String ownerRepo) {
        try {
            String url = GITHUB_API + "/repos/" + ownerRepo + "/readme";
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(createHeaders()), JsonNode.class
            );
            if (response.getBody() != null && response.getBody().has("content")) {
                String base64Content = response.getBody().get("content").asText();
                // GitHub API는 Base64로 인코딩된 값에 줄바꿈(\n)이 포함되어 있음
                base64Content = base64Content.replaceAll("\\s", "");
                return new String(Base64.getDecoder().decode(base64Content));
            }
        } catch (Exception e) {
            // README가 없는 레포
            return "";
        }
        return "";
    }

    // 최근 커밋 메시지 가져오기
    public List<String> getRecentCommits(String ownerRepo, int count) {
        String url = GITHUB_API + "/repos/" + ownerRepo + "/commits?per_page=" + count;
        ResponseEntity<JsonNode[]> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(createHeaders()), JsonNode[].class
        );
        List<String> messages = new ArrayList<>();
        if (response.getBody() != null) {
            for (JsonNode node : response.getBody()) {
                String message = node.get("commit").get("message").asText();
                messages.add(message);
            }
        }
        return messages;
    }
}
