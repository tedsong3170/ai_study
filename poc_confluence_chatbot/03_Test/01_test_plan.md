# 테스트 계획서 (Test Plan)

## 1. 개요

본 문서는 Confluence Chatbot 시스템의 테스트 전략 및 계획을 정의합니다.

### 1.1 테스트 목표

- 각 컴포넌트가 설계 명세대로 동작하는지 검증
- 컴포넌트 간 통합이 올바르게 이루어지는지 확인
- 사용자 시나리오 기반 End-to-End 테스트 수행

### 1.2 테스트 범위

| 컴포넌트 | 테스트 유형 |
|---------|-----------|
| Agent (Python) | 단위, 통합, API |
| Indexer (Kotlin) | 단위, 통합 |
| Chatbot (Kotlin) | 단위, 통합, API |
| UI (Flutter) | 위젯, 통합 |

---

## 2. 테스트 레벨

### 2.1 단위 테스트 (Unit Test)

개별 함수/클래스 수준의 테스트

| 컴포넌트 | 프레임워크 | 커버리지 목표 |
|---------|----------|-------------|
| Agent | pytest | 80% |
| Indexer | JUnit 5 + MockK | 70% |
| Chatbot | JUnit 5 + MockK | 70% |
| UI | flutter_test | 60% |

### 2.2 통합 테스트 (Integration Test)

컴포넌트 간 연동 테스트

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Chatbot   │────▶│    Agent    │────▶│  PostgreSQL │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   Ollama    │
                    └─────────────┘
```

### 2.3 E2E 테스트 (End-to-End Test)

사용자 시나리오 기반 전체 흐름 테스트

---

## 3. 테스트 케이스

### 3.1 Agent 서비스

#### 임베딩 서비스 (EmbeddingService)

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| EMB-001 | 텍스트 임베딩 시 384차원 벡터 반환 | High |
| EMB-002 | 빈 텍스트 임베딩 처리 | Medium |
| EMB-003 | 배치 임베딩 정상 동작 | High |
| EMB-004 | 동일 텍스트는 동일 벡터 반환 | Medium |

#### 청킹 서비스 (ChunkingService)

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| CHK-001 | 짧은 텍스트는 단일 청크 반환 | High |
| CHK-002 | 긴 텍스트는 max_size 이하로 분할 | High |
| CHK-003 | 청크 간 오버랩 적용 | Medium |
| CHK-004 | 빈/공백 텍스트는 빈 리스트 반환 | Medium |

#### 벡터 스토어 (VectorStore)

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| VEC-001 | 청크+임베딩 저장 | High |
| VEC-002 | 유사도 검색 결과 반환 | High |
| VEC-003 | 페이지 ID로 삭제 | High |
| VEC-004 | Space Key 필터링 검색 | Medium |

#### LLM 서비스 (LLMService)

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| LLM-001 | 컨텍스트 기반 답변 생성 | High |
| LLM-002 | **대화 이력(chat_history) 참조 답변** | High |
| LLM-003 | 빈 컨텍스트에서도 답변 생성 | Medium |

#### API 엔드포인트

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| API-001 | POST /embed 문서 임베딩 | High |
| API-002 | DELETE /embed/{id} 삭제 | High |
| API-003 | POST /query RAG 질의 | High |
| API-004 | GET /health 상태 확인 | High |

---

### 3.2 Indexer 서비스

#### Confluence 클라이언트

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| CFN-001 | Space 내 페이지 목록 조회 | High |
| CFN-002 | 페이지 상세 조회 | High |
| CFN-003 | HTML → 텍스트 변환 | High |
| CFN-004 | 페이지네이션 처리 | Medium |

#### Agent 클라이언트

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| AGT-001 | 임베딩 요청 성공 | High |
| AGT-002 | Agent 헬스체크 | High |

#### 색인 서비스

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| IDX-001 | 전체 색인 워크플로우 | High |
| IDX-002 | 빈 페이지 스킵 | Medium |
| IDX-003 | 실패 문서 카운트 | Medium |

---

### 3.3 Chatbot 서비스

#### 대화 관리

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| CHT-001 | 새 대화 생성 | High |
| CHT-002 | 대화 이력 저장 | High |
| CHT-003 | 대화 이력 조회 | High |
| CHT-004 | **이전 대화 참조 질문 처리** | High |

#### API 엔드포인트

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| CHA-001 | POST /api/chat 질문 응답 | High |
| CHA-002 | GET /api/chat/history 이력 조회 | High |

---

### 3.4 UI (Flutter)

| ID | 테스트 케이스 | 우선순위 |
|----|-------------|---------|
| UI-001 | 메시지 입력 및 전송 | High |
| UI-002 | 응답 메시지 표시 | High |
| UI-003 | 출처 링크 클릭 | Medium |
| UI-004 | 대화 이력 표시 | Medium |

---

## 4. 통합 테스트 시나리오

### 4.1 문서 색인 흐름

```
1. Indexer 실행
2. Confluence에서 문서 목록 조회
3. 각 문서 조회 및 HTML 파싱
4. Agent에 임베딩 요청
5. PostgreSQL에 벡터 저장
→ 결과: 문서가 검색 가능한 상태
```

### 4.2 질의응답 흐름

```
1. UI에서 질문 입력
2. Chatbot API 호출
3. Chatbot → Agent /query 호출
4. Agent: 벡터 검색 → LLM 답변 생성
5. 답변 + 출처 반환
→ 결과: UI에 답변 표시
```

### 4.3 Conversation Memory 흐름

```
1. 첫 질문: "휴가 정책 알려줘"
2. 답변: "연차 15일, 병가 10일..."
3. 후속 질문: "그거 더 자세히"
4. chat_history와 함께 Agent 호출
5. "휴가 정책"을 참조해서 상세 답변
→ 결과: 맥락 이해된 답변
```

---

## 5. 테스트 환경

### 5.1 로컬 개발 환경

```yaml
# docker-compose.test.yml
services:
  postgres-test:
    image: pgvector/pgvector:pg15
    ports: ["5433:5432"]
    environment:
      POSTGRES_DB: test_db
      
  # Ollama는 호스트에서 실행
```

### 5.2 필수 사전 조건

- [x] PostgreSQL + pgvector 실행
- [x] Ollama 서버 실행 (gpt-oss:20b 모델)
- [ ] Confluence API 토큰 설정 (통합 테스트 시)

---

## 6. 테스트 실행

### Agent 테스트

```bash
cd sourcecode/agent
source venv/bin/activate
pytest tests/ -v --cov=app
```

### Indexer 테스트

```bash
cd sourcecode/indexer
./gradlew test
```

### Chatbot 테스트

```bash
cd sourcecode/chatbot
./gradlew test
```

### UI 테스트

```bash
cd sourcecode/ui
flutter test
```

---

## 7. 테스트 현황

| 컴포넌트 | 상태 | 통과 | 비고 |
|---------|------|-----|------|
| Agent | ✅ 완료 | 15/15 | pytest |
| Indexer | ✅ 완료 | 15/15 | JUnit 5 |
| Chatbot | ✅ 완료 | 8/8 | JUnit 5 |
| UI | ⏳ 대기 | - | Flutter 개발 예정 |
| 통합 테스트 | ⏳ 대기 | - | 전체 컴포넌트 완료 후 |

### 7.1 Agent 테스트 상세

```
tests/test_chunking_service.py     ✅ 5 passed
tests/test_embedding_service.py    ✅ 4 passed
tests/test_llm_service.py          ✅ 3 passed
tests/test_vector_store.py         ✅ 3 passed
─────────────────────────────────────────────
Total                              ✅ 15 passed (15.85s)
```

### 7.2 Indexer 테스트 상세

```
ConfluenceClientTest               ✅ 5 passed (HTML 파싱)
AgentClientTest                    ✅ 3 passed (모델, URL 파싱)
ModelsTest                         ✅ 5 passed (직렬화/역직렬화)
AppConfigTest                      ✅ 2 passed (설정 로드)
─────────────────────────────────────────────
Total                              ✅ 15 passed, BUILD SUCCESSFUL (8s)
```

### 7.3 Chatbot 테스트 상세

```
ConversationStoreTest              ✅ 6 passed
AppConfigTest                      ✅ 2 passed
─────────────────────────────────────────────
Total                              ✅ 8 passed, BUILD SUCCESSFUL (5s)
```

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-20 | Antigravity | 초안 작성 |
