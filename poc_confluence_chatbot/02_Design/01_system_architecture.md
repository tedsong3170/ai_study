# Confluence Chatbot - 시스템 아키텍처 설계

## 1. 개요

본 문서는 Confluence Chatbot 시스템의 전체 아키텍처를 정의합니다.

### 1.1 설계 원칙

- **마이크로서비스 기반**: 각 컴포넌트는 독립적으로 배포/확장 가능
- **비동기 처리**: Vert.x 기반 논블로킹 I/O
- **RAG 패턴**: Retrieval-Augmented Generation을 통한 정확한 답변 생성
- **로컬 우선**: 모든 서비스가 로컬 환경에서 동작

---

## 2. 시스템 구성도

```mermaid
graph TB
    subgraph External["외부 시스템"]
        CONF["☁️ Confluence Cloud<br/>(Atlassian API)"]
    end
    
    subgraph LocalEnv["로컬 개발 환경"]
        subgraph Frontend["프론트엔드"]
            UI["📱 Flutter App<br/>(macOS/Android)"]
        end
        
        subgraph Backend["백엔드 서비스 (Docker)"]
            CHATBOT["💬 Chatbot Service<br/>Kotlin + Vert.x<br/>:8080"]
            INDEXER["📥 Indexer Service<br/>Kotlin + Vert.x<br/>:8081"]
        end
        
        subgraph AILayer["AI 레이어"]
            AGENT["🤖 Agent Service<br/>Python + LangChain<br/>:8000 (Docker)"]
            OLLAMA["🧠 Ollama<br/>gpt-oss:20b<br/>:11434 (Host)"]
        end
        
        subgraph DataLayer["데이터 레이어 (Docker)"]
            POSTGRES["🗄️ PostgreSQL<br/>+ pg-vector<br/>:5432"]
        end
    end
    
    UI <-->|"REST API"| CHATBOT
    CHATBOT <-->|"HTTP API"| AGENT
    INDEXER -->|"HTTP API"| AGENT
    INDEXER -->|"Atlassian REST API"| CONF
    AGENT <-->|"SQL + Vector"| POSTGRES
    AGENT <-->|"LLM API<br/>host.docker.internal"| OLLAMA
```

---

## 3. 컴포넌트 상세

### 3.1 Indexer Service

| 항목 | 내용 |
|-----|------|
| **역할** | Confluence 문서를 수집하고 벡터화하여 저장 |
| **기술 스택** | Kotlin 1.9, Vert.x 4.5.24 |
| **실행 방식** | 배치 애플리케이션 (수동/스케줄 실행) |
| **포트** | 8081 |

#### 주요 모듈

```
indexer/
├── src/main/kotlin/
│   ├── ConfluenceClient.kt      # Atlassian API 클라이언트
│   ├── DocumentParser.kt        # HTML → 텍스트 변환
│   ├── AgentClient.kt           # Agent API 호출
│   ├── IndexerService.kt        # 색인 비즈니스 로직
│   └── BatchRunner.kt           # 배치 실행 진입점
└── build.gradle.kts
```

#### 처리 흐름

```mermaid
sequenceDiagram
    participant B as BatchRunner
    participant C as ConfluenceClient
    participant P as DocumentParser
    participant A as AgentClient
    
    B->>C: 1. Space 내 문서 목록 조회
    C-->>B: 문서 ID 리스트
    
    loop 각 문서에 대해
        B->>C: 2. 문서 상세 조회
        C-->>B: 문서 HTML
        B->>P: 3. HTML 파싱
        P-->>B: 텍스트 + 메타데이터
        B->>A: 4. 임베딩 요청 (POST /embed)
        A-->>B: 저장 완료
    end
```

---

### 3.2 Chatbot Service

| 항목 | 내용 |
|-----|------|
| **역할** | 사용자 질문을 받아 Agent를 통해 답변 생성 |
| **기술 스택** | Kotlin 1.9, Vert.x 4.5.24 |
| **실행 방식** | 상시 구동 API 서버 |
| **포트** | 8080 |

#### 주요 모듈

```
chatbot/
├── src/main/kotlin/
│   ├── HttpServer.kt            # Vert.x HTTP 서버
│   ├── ChatController.kt        # REST API 핸들러
│   ├── AgentClient.kt           # Agent API 호출
│   ├── ConversationStore.kt     # 대화 이력 관리 (메모리)
│   └── Application.kt           # 애플리케이션 진입점
└── build.gradle.kts
```

#### API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chat` | 질문 전송 및 답변 수신 |
| GET | `/api/chat/history` | 대화 이력 조회 |
| GET | `/health` | 헬스체크 |

#### Conversation Memory 흐름

사용자의 이전 대화를 참조하여 맥락을 이해하는 기능입니다.

```mermaid
sequenceDiagram
    participant USER as User
    participant UI as Flutter UI
    participant CHT as Chatbot
    participant STORE as ConversationStore
    participant AGT as Agent
    participant LLM as Ollama
    
    USER->>UI: "그거 더 자세히 설명해줘"
    UI->>CHT: POST /api/chat (question + conversation_id)
    
    CHT->>STORE: 대화 이력 조회 (최근 5턴)
    STORE-->>CHT: chat_history 반환
    
    CHT->>AGT: POST /query (question + chat_history)
    
    Note over AGT: RAG 검색 + 대화 이력을<br/>LLM 프롬프트에 포함
    
    AGT->>LLM: 프롬프트 (컨텍스트 + 대화 이력 + 질문)
    LLM-->>AGT: "그거"가 무엇인지 이해한 답변
    
    AGT-->>CHT: 답변 + 출처
    CHT->>STORE: 질문/답변 저장
    CHT-->>UI: 응답
    UI-->>USER: 답변 표시
```

> [!NOTE]
> **Conversation Memory 설정**
> - 최근 5턴(10개 메시지)까지 컨텍스트로 사용
> - 토큰 제한 관리를 위해 오래된 대화는 요약하거나 제외

---

### 3.3 Agent Service

| 항목 | 내용 |
|-----|------|
| **역할** | RAG 파이프라인 실행 (임베딩, 검색, 답변 생성) |
| **기술 스택** | Python 3.12, LangChain, FastAPI |
| **실행 방식** | 상시 구동 API 서버 |
| **포트** | 8000 |

#### 주요 모듈

```
agent/
├── app/
│   ├── main.py                  # FastAPI 애플리케이션
│   ├── api/
│   │   ├── embed.py             # 임베딩 API
│   │   └── query.py             # 질의 API
│   ├── services/
│   │   ├── embedding_service.py # sentence-transformers 래퍼
│   │   ├── vector_store.py      # pg-vector 연동
│   │   └── llm_service.py       # Ollama 연동
│   ├── chains/
│   │   └── rag_chain.py         # LangChain RAG 체인
│   └── config.py                # 설정 관리
├── requirements.txt
└── Dockerfile
```

#### API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/embed` | 문서 임베딩 및 저장 |
| POST | `/query` | 질문에 대한 RAG 답변 생성 |
| GET | `/health` | 헬스체크 |

---

### 3.4 UI (Flutter App)

| 항목 | 내용 |
|-----|------|
| **역할** | 사용자 인터페이스 제공 |
| **기술 스택** | Flutter (Dart) |
| **지원 플랫폼** | macOS, Android |

#### 주요 화면

```
ui/
├── lib/
│   ├── main.dart
│   ├── screens/
│   │   └── chat_screen.dart     # 메인 채팅 화면
│   ├── widgets/
│   │   ├── message_bubble.dart  # 메시지 버블
│   │   ├── input_field.dart     # 입력 필드
│   │   └── source_link.dart     # 출처 링크
│   ├── services/
│   │   └── chat_api.dart        # Chatbot API 클라이언트
│   └── models/
│       └── message.dart         # 메시지 모델
└── pubspec.yaml
```

---

## 4. 데이터 레이어

### 4.1 PostgreSQL + pg-vector

| 항목 | 내용 |
|-----|------|
| **버전** | PostgreSQL 15+ |
| **확장** | pgvector |
| **포트** | 5432 |

#### 벡터 저장소 설계

```sql
-- pg-vector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 문서 청크 테이블
CREATE TABLE document_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    confluence_page_id VARCHAR(50) NOT NULL,
    confluence_space_key VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding vector(384),  -- sentence-transformers 차원
    page_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 벡터 검색용 인덱스
CREATE INDEX idx_embedding ON document_chunks 
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- 페이지 ID + 청크 인덱스 유니크 제약
CREATE UNIQUE INDEX idx_page_chunk 
ON document_chunks(confluence_page_id, chunk_index);
```

---

## 5. 통신 프로토콜

### 5.1 서비스 간 통신

```mermaid
graph LR
    UI -->|"HTTP/REST<br/>JSON"| CHATBOT
    CHATBOT -->|"HTTP/REST<br/>JSON"| AGENT
    INDEXER -->|"HTTP/REST<br/>JSON"| AGENT
    INDEXER -->|"HTTPS/REST<br/>JSON"| CONFLUENCE
    AGENT -->|"TCP<br/>SQL"| POSTGRES
    AGENT -->|"HTTP/REST<br/>JSON"| OLLAMA
```

### 5.2 포트 할당

| 서비스 | 포트 | 프로토콜 |
|-------|------|----------|
| Chatbot | 8080 | HTTP |
| Indexer | 8081 | HTTP |
| Agent | 8000 | HTTP |
| PostgreSQL | 5432 | TCP |
| Ollama | 11434 | HTTP |

---

## 6. 배포 구성

### 6.1 Docker Compose 구성

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg15
    environment:
      POSTGRES_DB: confluence_chatbot
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  # Ollama는 호스트에 설치하여 사용 (Docker 내 GPU 가속 제한으로 인한 성능 이슈)
  # 호스트에서 실행: ollama serve
  # 모델 다운로드: ollama pull gpt-oss:20b

  agent:
    build: ./agent
    ports:
      - "8000:8000"
    environment:
      DATABASE_URL: postgresql://postgres:postgres@postgres:5432/confluence_chatbot
      # macOS/Windows Docker Desktop: host.docker.internal 사용
      # Linux: --add-host=host.docker.internal:host-gateway 옵션 필요
      OLLAMA_BASE_URL: http://host.docker.internal:11434
    depends_on:
      - postgres
    extra_hosts:
      - "host.docker.internal:host-gateway"  # Linux 지원

  chatbot:
    build: ./chatbot
    ports:
      - "8080:8080"
    environment:
      AGENT_BASE_URL: http://agent:8000
    depends_on:
      - agent

  indexer:
    build: ./indexer
    ports:
      - "8081:8081"
    environment:
      AGENT_BASE_URL: http://agent:8000
      CONFLUENCE_BASE_URL: ${CONFLUENCE_BASE_URL}
      CONFLUENCE_API_TOKEN: ${CONFLUENCE_API_TOKEN}
      CONFLUENCE_SPACE_KEY: ${CONFLUENCE_SPACE_KEY}
    depends_on:
      - agent

volumes:
  postgres_data:
```

> [!IMPORTANT]
> **Ollama 호스트 설치 필수**  
> Docker 내에서 Ollama를 실행하면 GPU 가속이 제한되어 성능이 크게 저하됩니다.  
> 반드시 호스트에 Ollama를 설치하고 `ollama serve`로 실행해주세요.

### 6.2 Ollama 호스트 설치 및 실행

```bash
# macOS에 Ollama 설치
brew install ollama

# Ollama 서버 시작 (백그라운드)
ollama serve &

# 모델 다운로드
ollama pull gpt-oss:20b

# 모델 확인
ollama list
```

## 7. 보안 고려사항

### 7.1 인증 정보 관리

| 항목 | 저장 위치 | 접근 방식 |
|-----|----------|----------|
| Confluence API Token | `.env` 파일 | 환경변수 주입 |
| DB 비밀번호 | `.env` 파일 | 환경변수 주입 |

### 7.2 네트워크 보안

- 모든 서비스는 **로컬 네트워크** 내에서만 통신
- Confluence API 호출만 외부 HTTPS 통신

---

## 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|-----|------|-------|----------|
| 0.1 | 2026-01-19 | Antigravity | 초안 작성 |
