# Confluence Chatbot - 시스템 컨텍스트 (System Context)

## 1. 개요
본 문서는 Confluence Chatbot 시스템의 전체 컨텍스트와 외부 시스템과의 관계를 정의합니다.

---

## 2. 시스템 컨텍스트 다이어그램

```mermaid
graph TB
    subgraph External["외부 시스템"]
        CONF["Confluence Cloud"]
        OLLAMA["Ollama LLM Server"]
    end
    
    subgraph Users["사용자"]
        USER["End User"]
    end
    
    subgraph System["Confluence Chatbot System"]
        UI["UI (Flutter)"]
        CHATBOT["Chatbot Service"]
        INDEXER["Indexer Service"]
        AGENT["Agent Service"]
        PGVECTOR["PostgreSQL + pg-vector"]
    end
    
    USER -->|질문 입력| UI
    UI -->|웹/앱| USER
    
    UI <-->|REST API| CHATBOT
    CHATBOT <-->|HTTP API| AGENT
    
    INDEXER -->|Atlassian API| CONF
    INDEXER <-->|HTTP API| AGENT
    
    AGENT <-->|벡터 저장/검색| PGVECTOR
    AGENT <-->|LLM 추론| OLLAMA
```

---

## 3. 외부 시스템 인터페이스

### 3.1 Confluence Cloud

| 항목 | 내용 |
|-----|------|
| **연동 목적** | 문서 조회 및 색인 |
| **프로토콜** | HTTPS REST API |
| **인증 방식** | API Token (Basic Auth) 또는 OAuth 2.0 |
| **API 버전** | Atlassian REST API v2 |
| **주요 엔드포인트** | `/wiki/rest/api/content`, `/wiki/rest/api/space` |

### 3.2 Ollama LLM Server

| 항목 | 내용 |
|-----|------|
| **연동 목적** | 자연어 처리 및 답변 생성 |
| **프로토콜** | HTTP REST API |
| **기본 포트** | 11434 |
| **배포 위치** | 로컬 서버 |
| **API 형식** | OpenAI 호환 API |

---

## 4. 내부 컴포넌트 관계

```mermaid
graph LR
    subgraph Kotlin["Kotlin Services"]
        INDEXER["Indexer<br/>(Batch)"]
        CHATBOT["Chatbot<br/>(Async API)"]
    end
    
    subgraph Python["Python Service"]
        AGENT["Agent<br/>(LangChain + RAG)"]
    end
    
    subgraph Flutter["Flutter App"]
        UI["UI"]
    end
    
    subgraph Data["Data Layer"]
        PG["PostgreSQL<br/>+ pg-vector"]
    end
    
    INDEXER -->|POST /embed| AGENT
    CHATBOT -->|POST /query| AGENT
    UI <-->|REST API| CHATBOT
    AGENT <-->|SQL| PG
```

---

## 5. 데이터 흐름

### 5.1 문서 색인 흐름 (Indexing Flow)

```mermaid
sequenceDiagram
    participant SCH as Scheduler
    participant IDX as Indexer
    participant CONF as Confluence
    participant AGT as Agent
    participant DB as pg-vector
    
    SCH->>IDX: 색인 작업 트리거
    IDX->>CONF: 문서 목록 조회
    CONF-->>IDX: 문서 리스트 반환
    
    loop 각 문서에 대해
        IDX->>CONF: 문서 상세 조회
        CONF-->>IDX: 문서 내용 반환
        IDX->>AGT: 문서 분석/임베딩 요청
        AGT->>AGT: 청킹 + 임베딩 생성
        AGT->>DB: 벡터 저장
        DB-->>AGT: 저장 완료
        AGT-->>IDX: 색인 완료
    end
    
    IDX-->>SCH: 배치 완료
```

### 5.2 질문 응답 흐름 (Query Flow)

```mermaid
sequenceDiagram
    participant USER as User
    participant UI as Flutter UI
    participant CHT as Chatbot
    participant AGT as Agent
    participant DB as pg-vector
    participant LLM as Ollama
    
    USER->>UI: 질문 입력
    UI->>CHT: POST /api/chat
    CHT->>AGT: POST /query
    
    AGT->>AGT: 질문 임베딩 생성
    AGT->>DB: 유사 문서 검색
    DB-->>AGT: 관련 문서 반환
    
    AGT->>LLM: 프롬프트 + 컨텍스트
    LLM-->>AGT: 답변 생성
    
    AGT-->>CHT: 답변 + 출처
    CHT-->>UI: JSON 응답
    UI-->>USER: 답변 표시
```

---

## 6. 배포 컨텍스트

```mermaid
graph TB
    subgraph Dev["개발 환경"]
        DEV_UI["Flutter (macOS)"]
        DEV_CHATBOT["Chatbot (localhost:8080)"]
        DEV_INDEXER["Indexer (localhost:8081)"]
        DEV_AGENT["Agent (localhost:8000)"]
        DEV_PG["PostgreSQL (localhost:5432)"]
        DEV_OLLAMA["Ollama (localhost:11434)"]
    end
    
    DEV_UI --> DEV_CHATBOT
    DEV_CHATBOT --> DEV_AGENT
    DEV_INDEXER --> DEV_AGENT
    DEV_AGENT --> DEV_PG
    DEV_AGENT --> DEV_OLLAMA
```

---

## 7. 주요 액터 정의

| 액터 | 역할 | 설명 |
|-----|------|------|
| **End User** | 질문자 | Confluence 문서에 대해 질문하는 사용자 |
| **Scheduler** | 트리거 | 색인 배치 작업을 주기적으로 실행 |
| **Admin** | 관리자 | 시스템 설정 및 모니터링 담당 (향후) |

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-19 | Antigravity | 초안 작성 |
