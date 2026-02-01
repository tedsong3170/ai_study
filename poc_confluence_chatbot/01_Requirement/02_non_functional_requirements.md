# Confluence Chatbot - 비기능 요구사항 (Non-Functional Requirements)

## 1. 개요
본 문서는 Confluence Chatbot 프로젝트의 비기능 요구사항을 정의합니다.

---

## 2. 성능 요구사항 (Performance)

| ID | 요구사항 | 목표값 | 비고 |
|----|---------|-------|------|
| NFR-P001 | 질문 응답 시간 | < 10초 | 일반적인 질문 기준 |
| NFR-P002 | 동시 사용자 처리 | 1~2명 | POC 기준, 로컬 LLM 사용 |
| NFR-P003 | 문서 색인 처리량 | 100문서/분 | 배치 처리 기준 |
| NFR-P004 | API 응답 시간 | < 500ms | Agent API 제외 |

---

## 3. 확장성 요구사항 (Scalability)

| ID | 요구사항 | 설명 |
|----|---------|------|
| NFR-S001 | 수평 확장 가능 | Chatbot 서비스는 인스턴스 추가로 확장 가능해야 한다 |
| NFR-S002 | 비동기 처리 | 높은 부하에도 비동기 처리로 안정성 유지 |
| NFR-S003 | 벡터 DB 확장 | 문서 증가에 따른 pg-vector 확장 고려 |

---

## 4. 가용성 요구사항 (Availability)

| ID | 요구사항 | 목표값 | 비고 |
|----|---------|-------|------|
| NFR-A001 | 서비스 가용률 | 99% | POC 기준, 평일 업무시간 |
| NFR-A002 | 장애 복구 시간 | < 1시간 | 수동 복구 허용 |

---

## 5. 보안 요구사항 (Security)

| ID | 요구사항 | 설명 | 우선순위 |
|----|---------|------|----------|
| NFR-SEC001 | API 인증 | Confluence API 접근 시 토큰 기반 인증 사용 | 필수 |
| NFR-SEC002 | 통신 암호화 | 서비스 간 통신은 HTTPS/TLS 사용 | 권장 |
| NFR-SEC003 | 민감정보 관리 | API 키, 토큰 등은 환경변수로 관리 | 필수 |
| NFR-SEC004 | 로그 마스킹 | 로그에 민감 정보 노출 금지 | 권장 |

---

## 6. 운영 요구사항 (Operability)

| ID | 요구사항 | 설명 |
|----|---------|------|
| NFR-O001 | 로깅 | 구조화된 로그 출력 (JSON 형식 권장) |
| NFR-O002 | 헬스체크 | 각 서비스의 상태 확인 API 제공 |
| NFR-O003 | 설정 외부화 | 환경별 설정을 외부 파일/환경변수로 관리 |
| NFR-O004 | 컨테이너화 | Docker 기반 배포 지원 |

---

## 7. 개발 요구사항 (Development)

| ID | 요구사항 | 설명 |
|----|---------|------|
| NFR-D001 | TDD 적용 | 테스트 주도 개발 방법론 적용 |
| NFR-D002 | 코드 품질 | Lint, 정적 분석 도구 적용 |
| NFR-D003 | 문서화 | API 문서 자동 생성 (OpenAPI/Swagger) |
| NFR-D004 | 버전 관리 | Git 기반 형상 관리 |

---

## 8. 기술 스택 제약사항 (Technology Constraints)

### 8.1 Indexer / Chatbot
- **언어**: Kotlin 1.9
- **프레임워크**: Vert.x 4.5.24
- **빌드 도구**: Gradle (권장)

### 8.2 Agent
- **언어**: Python 3.12
- **AI 프레임워크**: LangChain
- **LLM**: Ollama (로컬 LLM)
- **벡터 DB**: PostgreSQL + pg-vector
- **임베딩**: sentence-transformers

### 8.3 UI
- **프레임워크**: Flutter
- **지원 플랫폼**: macOS, Android

---

## 9. 인터페이스 요구사항 (Interface)

| ID | 요구사항 | 설명 |
|----|---------|------|
| NFR-I001 | REST API | Chatbot-UI 간 HTTP REST API 사용 |
| NFR-I002 | Agent API | Kotlin 서비스 - Python Agent 간 HTTP API |
| NFR-I003 | Confluence API | Atlassian REST API v2 사용 |

---

## 10. 확정된 결정사항

> [!NOTE]
> 아래 항목들은 사용자 확인 완료된 사항입니다.

| 항목 | 결정 사항 | 비고 |
|-----|----------|------|
| **Ollama 모델** | gpt-oss 20b | 로컬 LLM |
| **배포 환경** | 로컬 개발환경 only | Docker 기반 |
| **데이터 보존 기간** | POC 기간 동안 | 별도 정책 불필요 |
| **다국어 지원** | 한국어 우선 | 영어 문서도 처리 가능 |

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-19 | Antigravity | 초안 작성 |
