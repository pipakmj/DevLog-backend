package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.request.AiFeedbackRequest;
import com.devlog.devlog.auth.dto.portfolio.response.AiFeedbackResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
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

        @Retryable(
                retryFor = HttpServerErrorException.class,
                noRetryFor = HttpClientErrorException.class,
                maxAttempts = 3,
                backoff = @Backoff(delay = 2000, multiplier = 2)
        )
        public AiFeedbackResponse PortfolioAiFeedback(AiFeedbackRequest request) {
                String prompt = buildFeedbackPrompt(request);
                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text", prompt)
                                ))
                        )
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-goog-api-key", apiKey);

                URI uri = URI.create(GEMINI_URL);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        uri, HttpMethod.POST, entity, JsonNode.class
                );
                if (response.getBody() != null) {
                        try {
                                String jsonText = response.getBody()
                                        .get("candidates").get(0)
                                        .get("content")
                                        .get("parts").get(0)
                                        .get("text").asText();
                                // 간혹 AI가 텍스트 앞뒤로 ```json ... ``` 을 붙이는 경우가 있으므로 방어 코드 작성
                                jsonText = jsonText
                                        .replaceAll("```json", "")
                                        .replaceAll("```", "")
                                        .trim();

                                // 2. 받아온 JSON 텍스트를 미리 설계한 DTO 객체 구조로 역직렬화(Deserialization)
                                ObjectMapper objectMapper = new ObjectMapper();
                                return objectMapper.readValue(jsonText, AiFeedbackResponse.class);
                        } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException("AI 피드백 결과를 처리하는 중 오류가 발생했습니다.");
                        }
                }
                throw new RuntimeException("AI 피드백 생성에 실패했습니다.");
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

        // Gemini가 결과물을 지정된 JSON 스펙으로 내려주도록 하는 프롬프트 빌더
        private String buildFeedbackPrompt(AiFeedbackRequest request) {
                // 구조가 복잡하므로 ObjectMapper 등을 이용해 request 객체를 JSON 문자열로 변환하여 주입하면 편리합니다.
                String requestJson = "";
                try {
                        requestJson = new ObjectMapper().writeValueAsString(request);
                } catch (JsonProcessingException e) {
                        requestJson = "데이터 파싱 실패";
                }

                return """
                                다음은 사용자가 입력 중인 포트폴리오 데이터입니다.
                                
                                [포트폴리오 데이터]
                                %s
                                
                                위 데이터를 기반으로 포트폴리오를 진단하고 개선 사항을 도출해 주세요.
                                반드시 아래 구조의 **순수 JSON 포맷으로만** 답변해야 합니다. 마크다운 기호(```json)나 앞뒤 설명은 절대 포함하지 마세요.
                                
                                {
                                  "score": 0~100 사이의 정수 점수,
                                  "missingSections": ["부족하거나 더 채워야 할 세션명 영문 리스트(예: troubleshoots, metrics)"],
                                  "suggestions": ["구체적인 피드백 문장 1", "구체적인 피드백 문장 2"],
                                  "autoCompletedFields": {
                                    "metrics": "데이터를 기반으로 AI가 제안하는 모범적인 정량적 성과/지표 예시 문장 (한국어)",
                                    "troubleshoots": [
                                      {
                                        "issue": "부족한 부분에 대한 예상 발생 문제 기술",
                                        "resolution": "해당 문제를 해결하기 위한 구체적인 기술적 접근법 제안"
                                      }
                                    ]
                                  }
                                }
                                """.formatted(requestJson);
        }
}
