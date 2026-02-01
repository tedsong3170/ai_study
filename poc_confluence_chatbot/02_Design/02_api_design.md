# Confluence Chatbot - API 설계

## 1. 개요

본 문서는 Confluence Chatbot 시스템의 API 명세를 정의합니다.

---

## 2. Chatbot Service API

### Base URL
```
http://localhost:8080
```

---

### 2.1 채팅 API

#### POST /api/chat

사용자 질문에 대한 답변을 생성합니다.

**Request**
```json
{
  "question": "string",
  "conversation_id": "string (optional)"
}
```

**Response**
```json
{
  "answer": "string",
  "sources": [
    {
      "title": "string",
      "url": "string",
      "relevance_score": 0.95
    }
  ],
  "conversation_id": "string"
}
```

**Status Codes**
| Code | Description |
|------|-------------|
| 200 | 성공 |
| 400 | 잘못된 요청 (question 누락 등) |
| 500 | 서버 오류 |
| 503 | Agent 서비스 연결 실패 |

**Example**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "우리 회사의 휴가 정책은 어떻게 되나요?"
  }'
```

---

#### GET /api/chat/history

대화 이력을 조회합니다.

**Query Parameters**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| conversation_id | string | Yes | 대화 세션 ID |
| limit | integer | No | 조회할 메시지 수 (기본: 20) |

**Response**
```json
{
  "conversation_id": "string",
  "messages": [
    {
      "role": "user",
      "content": "string",
      "timestamp": "2026-01-19T10:00:00Z"
    },
    {
      "role": "assistant",
      "content": "string",
      "sources": [...],
      "timestamp": "2026-01-19T10:00:05Z"
    }
  ]
}
```

---

#### GET /health

서비스 상태를 확인합니다.

**Response**
```json
{
  "status": "healthy",
  "version": "0.1.0",
  "dependencies": {
    "agent": "healthy"
  }
}
```

---

## 3. Agent Service API

### Base URL
```
http://localhost:8000
```

---

### 3.1 임베딩 API

#### POST /embed

문서를 임베딩하여 벡터 DB에 저장합니다.

**Request**
```json
{
  "page_id": "string",
  "space_key": "string",
  "title": "string",
  "content": "string",
  "page_url": "string"
}
```

**Response**
```json
{
  "status": "success",
  "page_id": "string",
  "chunks_created": 5,
  "message": "Document embedded successfully"
}
```

**Status Codes**
| Code | Description |
|------|-------------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 500 | 임베딩/저장 실패 |

**Example**
```bash
curl -X POST http://localhost:8000/embed \
  -H "Content-Type: application/json" \
  -d '{
    "page_id": "12345",
    "space_key": "DEV",
    "title": "개발 가이드",
    "content": "이 문서는 개발 가이드입니다...",
    "page_url": "https://company.atlassian.net/wiki/spaces/DEV/pages/12345"
  }'
```

---

#### DELETE /embed/{page_id}

특정 페이지의 임베딩을 삭제합니다.

**Path Parameters**
| Parameter | Type | Description |
|-----------|------|-------------|
| page_id | string | Confluence 페이지 ID |

**Response**
```json
{
  "status": "success",
  "page_id": "string",
  "chunks_deleted": 5
}
```

---

### 3.2 질의 API

#### POST /query

질문에 대한 RAG 기반 답변을 생성합니다.  
**Conversation Memory**: `chat_history`를 전달하면 이전 대화를 참조하여 답변합니다.

**Request**
```json
{
  "question": "string",
  "top_k": 5,
  "space_key": "string (optional)",
  "chat_history": [
    {
      "role": "user",
      "content": "이전 질문"
    },
    {
      "role": "assistant", 
      "content": "이전 답변"
    }
  ]
}
```

> [!NOTE]
> **Conversation Memory 동작 방식**
> 1. `chat_history`가 전달되면 LLM 프롬프트에 이전 대화 컨텍스트가 포함됩니다.
> 2. 대화 이력은 최근 N개(기본: 5턴)만 사용하여 토큰 제한을 관리합니다.
> 3. 사용자가 "그거", "방금 말한", "위에서" 등의 지시어를 사용해도 맥락을 이해합니다.

**Response**
```json
{
  "answer": "string",
  "sources": [
    {
      "page_id": "string",
      "title": "string",
      "url": "string",
      "chunk_content": "string",
      "relevance_score": 0.95
    }
  ],
  "processing_time_ms": 1500
}
```

**Status Codes**
| Code | Description |
|------|-------------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 404 | 관련 문서 없음 |
| 500 | LLM 처리 실패 |
| 503 | Ollama 연결 실패 |

**Example**
```bash
curl -X POST http://localhost:8000/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "휴가 신청 절차가 어떻게 되나요?",
    "top_k": 3
  }'
```

---

#### GET /health

서비스 상태를 확인합니다.

**Response**
```json
{
  "status": "healthy",
  "version": "0.1.0",
  "dependencies": {
    "postgres": "healthy",
    "ollama": "healthy"
  },
  "model": "gpt-oss:20b"
}
```

---

## 4. Indexer Service API

### Base URL
```
http://localhost:8081
```

---

### 4.1 색인 관리 API

#### POST /api/index/run

색인 작업을 수동으로 실행합니다.

**Request**
```json
{
  "space_key": "string",
  "full_reindex": false
}
```

**Response**
```json
{
  "job_id": "string",
  "status": "started",
  "message": "Indexing job started"
}
```

---

#### GET /api/index/status

색인 작업 상태를 조회합니다.

**Query Parameters**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| job_id | string | No | 특정 작업 ID (없으면 최근 작업) |

**Response**
```json
{
  "job_id": "string",
  "status": "running | completed | failed",
  "progress": {
    "total_documents": 100,
    "processed_documents": 45,
    "failed_documents": 0
  },
  "started_at": "2026-01-19T10:00:00Z",
  "completed_at": null,
  "error": null
}
```

---

#### GET /health

서비스 상태를 확인합니다.

**Response**
```json
{
  "status": "healthy",
  "version": "0.1.0",
  "dependencies": {
    "agent": "healthy",
    "confluence": "healthy"
  }
}
```

---

## 5. 에러 응답 형식

모든 API는 에러 발생 시 일관된 형식으로 응답합니다.

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message",
    "details": {}
  }
}
```

### 공통 에러 코드

| Code | HTTP Status | Description |
|------|-------------|-------------|
| INVALID_REQUEST | 400 | 요청 형식 오류 |
| NOT_FOUND | 404 | 리소스 없음 |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |
| SERVICE_UNAVAILABLE | 503 | 의존 서비스 연결 실패 |
| TIMEOUT | 504 | 처리 시간 초과 |

---

## 6. API 인증

> [!NOTE]
> POC 단계에서는 인증을 적용하지 않습니다.  
> 향후 확장 시 API Key 또는 JWT 기반 인증 도입을 고려합니다.

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-19 | Antigravity | 초안 작성 |
