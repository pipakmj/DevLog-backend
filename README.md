# DevLog - 개발자 개인 포트폴리오 & 블로그 백엔드
DevLog는 개발자가 자신의 프로젝트를 관리하고, 블로그 포스트를 기록하며, 특히 **GitHub 저장소를 AI로 자동 분석**하여 포트폴리오 작성을 도와주는 스마트한 백엔드 서비스입니다.
## 주요 기능
### 인증 및 보안 (Auth)
- **JWT 기반 인증**: Access Token 및 Refresh Token을 활용한 안전한 로그인 시스템.
- **쿠키 기반 토큰 관리**: HttpOnly 설정을 통해 클라이언트 보안 강화.
- **Spring Security**: 상태가 없는(Stateless) 아키텍처와 정교한 엔드포인트 권한 제어.
### 프로젝트 관리 (Project Management)
- **자동 기술 스택 추출**: GitHub URL 입력 시 `build.gradle`, `package.json` 등을 분석하여 사용된 기술 자동 분류.
- **AI 요약 서비스**: Google Gemini LLM을 사용하여 저장소의 README와 최근 커밋 메시지를 바탕으로 프로젝트 설명 및 주요 기능 자동 생성.
- **유연한 조회**: 무한 스크롤을 위한 `Slice` 페이징과 포스트 연결을 위한 전체 리스트 조회 지원.
### 블로그 포스트 (Post & Tag)
- **포스트 CRUD**: 마크다운 기반의 글쓰기 및 태그 관리 기능.
- **조회수 트래킹**: 실시간 포스트 조회수 업데이트 및 상세 조회 기능.
- **태그 시스템**: 다대다 관계 설정을 통한 효율적인 태그 분류.
## 🛠 Tech Stack
### Framework & Language
- **Java 21**
- **Spring Boot 3.5.x**
- **Spring Data JPA**
- **Spring Security**
### Database & Storage
- **MySQL 8.0**
- **Redis** (Refresh Token 관리 예정)
### AI & API Integration
- **Google Gemini API** (v2.5 Flash 모델)
- **GitHub REST API**
### Tools
- **Gradle**
- **Lombok**
- **Postman** (API 검증)
