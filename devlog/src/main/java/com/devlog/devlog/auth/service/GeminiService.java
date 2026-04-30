package com.devlog.devlog.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

        private final RestTemplate restTemplate;

        @Value("${GEMINI_API_KEY}")
        private String apiKey;

        private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

        @Retryable(
                retryFor = HttpServerErrorException.class,
                noRetryFor = HttpClientErrorException.class,
                maxAttempts = 3,
                backoff = @Backoff(delay = 2000, multiplier = 2)
        )
        public String summarize(String readme, List<String> commits) {
                String prompt = buildPrompt(readme, commits);
                // Gemini API 요청 바디 구성
                Map<String, Object> requestBody = Map.of(
                                "contents", List.of(
                                                Map.of("parts", List.of(
                                                                Map.of("text", prompt)))));
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-goog-api-key", apiKey);

                // URL 문자열 대신 URI 객체를 직접 사용하여 콜론(:) 문자가 템플릿 변수로 오해받지 않도록 합니다.
                URI uri = URI.create(GEMINI_URL);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                                uri, HttpMethod.POST, entity, JsonNode.class);
                // 응답에서 텍스트 추출
                if (response.getBody() != null) {
                        return response.getBody()
                                        .get("candidates").get(0)
                                        .get("content")
                                        .get("parts").get(0)
                                        .get("text").asText();
                }
                return "";
        }

        private String buildPrompt(String readme, List<String> commits) {
                String commitText = String.join("\n- ", commits);
                return """
                                다음은 GitHub 프로젝트의 README 내용과 최근 커밋 메시지입니다.

                                [README]
                                %s

                                [최근 커밋 메시지]
                                - %s

                                위 정보를 바탕으로 아래 형식에 맞춰 **정확히 JSON만** 응답해주세요.
                                다른 설명이나 마크다운 없이 순수 JSON만 출력하세요.
                                techStack을 뽑을 땐 README에 명시된 기술만 뽑아주세요.

                                {
                                  "description": "프로젝트 설명 (2~3문장, 한국어)",
                                  "techStack": ["기술 스택1", "기술 스택2", "기술 스택3"],
                                  "features": ["주요 기능1", "주요 기능2", "주요 기능3"]
                                }
                                """.formatted(readme, commitText);
        }
}
