# 통합 테스트 계획서 (Integration Test Plan)

## 1. 개요

본 문서는 Confluence Chatbot 시스템의 통합 테스트 계획을 정의합니다.

### 1.1 목적

- 개별 컴포넌트 간 연동이 올바르게 동작하는지 검증
- End-to-End 시나리오 기반 흐름 테스트
- Conversation Memory 기능의 실제 동작 확인

### 1.2 테스트 대상

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Indexer    │─────▶│    Agent     │◀─────│   Chatbot    │
│   (Kotlin)   │      │   (Python)   │      │   (Kotlin)   │
└──────────────┘      └──────┬───────┘      └──────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                              ▼
       ┌──────────────┐              ┌──────────────┐
       │  PostgreSQL  │              │    Ollama    │
       │  + pgvector  │              │  gpt-oss:20b │
       └──────────────┘              └──────────────┘
```

---

## 2. 사전 조건

### 2.1 인프라

| 구성 요소 | 요구 사항 | 상태 |
|----------|----------|------|
| PostgreSQL + pgvector | 5432 포트 | [ ] |
| Ollama (gpt-oss:20b) | 11434 포트 | [ ] |
| Agent 서비스 | 8000 포트 | [ ] |
| Chatbot 서비스 | 8080 포트 | [ ] |

### 2.2 테스트 데이터

```sql
-- 테스트용 문서 청크 (pgvector)
INSERT INTO document_chunks 
(confluence_page_id, confluence_space_key, title, content, page_url)
VALUES 
('TEST-001', 'TEST', '휴가 정책', '연차휴가는 15일입니다. 병가는 10일입니다.', 'http://test/1'),
('TEST-002', 'TEST', '출장 규정', '출장비는 일비 10만원입니다.', 'http://test/2');
```

---

## 3. 통합 테스트 시나리오

### 3.1 문서 색인 흐름 (Indexer → Agent)

| ID | 시나리오 | 검증 항목 |
|----|---------|----------|
| INT-IDX-001 | Agent 헬스체크 | Indexer가 Agent /health 호출 성공 |
| INT-IDX-002 | 단일 문서 임베딩 | POST /embed 호출 후 chunks_created > 0 |
| INT-IDX-003 | 문서 삭제 | DELETE /embed/{id} 후 chunks_deleted 확인 |
| INT-IDX-004 | 배치 색인 | 여러 문서 연속 색인 성공 |

**테스트 절차:**
```bash
# 1. Agent 헬스체크
curl http://localhost:8000/health

# 2. 문서 임베딩
curl -X POST http://localhost:8000/embed \
  -H "Content-Type: application/json" \
  -d '{"page_id":"TEST-001","space_key":"TEST","title":"테스트","content":"테스트 내용","page_url":"http://test"}'

# 3. 임베딩 삭제  
curl -X DELETE http://localhost:8000/embed/TEST-001
```

---

### 3.2 질의응답 흐름 (Chatbot → Agent)

| ID | 시나리오 | 검증 항목 |
|----|---------|----------|
| INT-QA-001 | 기본 질의 | POST /api/chat → 답변 반환 |
| INT-QA-002 | 출처 포함 | sources 배열에 관련 문서 정보 |
| INT-QA-003 | 새 대화 생성 | conversation_id 자동 생성 |
| INT-QA-004 | 기존 대화 이어가기 | 동일 conversation_id로 연속 질문 |

**테스트 절차:**
```bash
# 1. 첫 질문 (새 대화)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"휴가 정책 알려줘"}'
# → conversation_id 확인

# 2. 후속 질문 (기존 대화)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"그거 더 자세히 알려줘","conversation_id":"<위에서 받은 ID>"}'
```

---

### 3.3 Conversation Memory 흐름 ⭐

| ID | 시나리오 | 검증 항목 |
|----|---------|----------|
| INT-MEM-001 | 대화 이력 저장 | messages 배열에 user/assistant 저장 |
| INT-MEM-002 | 이력 기반 답변 | "그거" 지시어 이해 |
| INT-MEM-003 | chat_history 전달 | Agent에 이전 대화 전달 확인 |
| INT-MEM-004 | 최대 턴 제한 | 5턴 이상 시 오래된 이력 제외 |

**테스트 절차:**
```bash
# Step 1: 첫 질문
RESPONSE1=$(curl -s -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"휴가 정책 알려줘"}')
CONV_ID=$(echo $RESPONSE1 | jq -r '.conversation_id')
echo "대화 ID: $CONV_ID"

# Step 2: 후속 질문 (맥락 참조)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"question\":\"그거 더 자세히 설명해줘\",\"conversation_id\":\"$CONV_ID\"}"
# → "휴가 정책"을 참조한 답변인지 확인

# Step 3: 대화 이력 조회
curl "http://localhost:8080/api/chat/history?conversation_id=$CONV_ID"
# → 4개 메시지 (user, assistant, user, assistant)
```

---

### 3.4 전체 흐름 E2E

| ID | 시나리오 | 검증 항목 |
|----|---------|----------|
| INT-E2E-001 | 문서 색인 → 질의 | 색인된 문서 기반 답변 생성 |
| INT-E2E-002 | 문서 업데이트 | 재색인 후 변경된 내용 반영 |
| INT-E2E-003 | 문서 삭제 | 삭제된 문서는 검색 안 됨 |

---

## 4. 테스트 환경 구성

### 4.1 Docker Compose (테스트용)

```yaml
# docker-compose.test.yml
version: '3.8'

services:
  postgres-test:
    image: pgvector/pgvector:pg15
    environment:
      POSTGRES_DB: test_db
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
    ports:
      - "5433:5432"
    volumes:
      - ./init-test.sql:/docker-entrypoint-initdb.d/init.sql

  agent:
    build: ./sourcecode/agent
    ports:
      - "8000:8000"
    environment:
      DATABASE_URL: postgresql+asyncpg://test:test@postgres-test:5432/test_db
      OLLAMA_BASE_URL: http://host.docker.internal:11434
    depends_on:
      - postgres-test

  chatbot:
    build: ./sourcecode/chatbot
    ports:
      - "8080:8080"
    environment:
      AGENT_BASE_URL: http://agent:8000
    depends_on:
      - agent
```

### 4.2 실행 명령어

```bash
# 1. Ollama 시작 (호스트)
ollama serve &
ollama pull gpt-oss:20b

# 2. 테스트 환경 시작
docker-compose -f docker-compose.test.yml up -d

# 3. 헬스체크 확인
curl http://localhost:8000/health
curl http://localhost:8080/health

# 4. 통합 테스트 실행
./scripts/integration_test.sh
```

---

## 5. 테스트 스크립트

### 5.1 자동화 스크립트 위치

```
03_Test/
├── 01_test_plan.md
├── 02_integration_test_plan.md  (본 문서)
└── scripts/
    ├── integration_test.sh      # 통합 테스트 실행
    ├── setup_test_data.sh       # 테스트 데이터 생성
    └── cleanup.sh               # 정리
```

---

## 6. 성공 기준

| 항목 | 기준 |
|-----|------|
| 문서 색인 | 모든 문서 정상 색인 |
| 질의응답 | 관련 문서 기반 답변 생성 |
| Conversation Memory | 이전 대화 참조 답변 |
| 응답 시간 | 10초 이내 |
| 에러율 | 0% |

---

## 7. 체크리스트

### 7.1 사전 준비

- [ ] PostgreSQL + pgvector 실행 확인
- [ ] Ollama 서버 실행 및 모델 로드 확인
- [ ] Agent 서비스 빌드 및 실행
- [ ] Chatbot 서비스 빌드 및 실행
- [ ] 테스트 데이터 준비

### 7.2 테스트 실행

- [ ] INT-IDX-001 ~ INT-IDX-004 (색인 흐름)
- [ ] INT-QA-001 ~ INT-QA-004 (질의 흐름)
- [ ] INT-MEM-001 ~ INT-MEM-004 (Conversation Memory)
- [ ] INT-E2E-001 ~ INT-E2E-003 (전체 흐름)

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-20 | Antigravity | 초안 작성 |
