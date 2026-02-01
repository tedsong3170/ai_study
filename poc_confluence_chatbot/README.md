# Confluence Chatbot

AI 기반 Confluence 문서 질의응답 시스템

## 🎯 개요

Confluence에 저장된 문서들을 RAG(Retrieval-Augmented Generation) 방식으로 검색하여 자연어로 질문에 답변하는 챗봇 시스템입니다. **Conversation Memory**를 지원하여 이전 대화 맥락을 이해하고 연속적인 대화가 가능합니다.

## 🏗️ 아키텍처

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Flutter    │─────▶│   Chatbot    │─────▶│    Agent     │
│     UI       │      │   (Kotlin)   │      │   (Python)   │
└──────────────┘      └──────────────┘      └──────┬───────┘
     :8080                 :8080                   :8000
                                                    │
              ┌────────────────────────────────────┴──────┐
              ▼                                           ▼
       ┌──────────────┐                           ┌──────────────┐
       │  PostgreSQL  │                           │    Ollama    │
       │  + pgvector  │                           │  gpt-oss:20b │
       └──────────────┘                           └──────────────┘
            :5432                                      :11434
```

## 📦 구성 요소

| 서비스 | 기술 스택 | 설명 |
|-------|----------|------|
| **Agent** | Python, FastAPI | 임베딩, 벡터 검색, LLM 호출 |
| **Chatbot** | Kotlin, Vert.x | 대화 관리, Conversation Memory |
| **Indexer** | Kotlin, Vert.x | Confluence 문서 색인 |
| **UI** | Flutter | 채팅 인터페이스 |
| **DB** | PostgreSQL + pgvector | 벡터 저장소 |
| **LLM** | Ollama (gpt-oss:20b) | 로컬 LLM |

## 🚀 빠른 시작

### 사전 요구사항

- Docker & Docker Compose
- Ollama (로컬 LLM)
- Flutter (UI 개발 시)

### 1. Ollama 설정

```bash
# Ollama 시작
ollama serve &

# 모델 다운로드
ollama pull gpt-oss:20b
```

### 2. 환경 변수 설정

```bash
cp .env.example .env
# 필요시 Confluence 설정 수정
```

### 3. 서비스 실행

```bash
# 전체 서비스 시작
docker-compose up -d

# 상태 확인
docker-compose ps
```

### 4. 헬스체크

```bash
curl http://localhost:8000/health  # Agent
curl http://localhost:8080/health  # Chatbot
```

## 📡 API 엔드포인트

### Chatbot API (포트 8080)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chat` | 질문 전송 및 답변 수신 |
| GET | `/api/chat/history` | 대화 이력 조회 |
| GET | `/health` | 헬스체크 |

#### 예시: 질문하기

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "휴가 정책 알려줘"}'
```

#### 예시: 후속 질문 (Conversation Memory)

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "그거 더 자세히 알려줘",
    "conversation_id": "<이전 응답의 conversation_id>"
  }'
```

### Agent API (포트 8000)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/embed` | 문서 임베딩 |
| DELETE | `/embed/{page_id}` | 임베딩 삭제 |
| POST | `/query` | RAG 질의 |
| GET | `/health` | 헬스체크 |

## 📁 프로젝트 구조

```
poc_confluence_chatbot/
├── 01_Requirement/          # 요구사항 문서
├── 02_Design/               # 설계 문서
├── 03_Test/                 # 테스트 계획 및 스크립트
├── sourcecode/
│   ├── agent/               # Python FastAPI 서비스
│   ├── chatbot/             # Kotlin Vert.x 서비스
│   ├── indexer/             # Kotlin 배치 서비스
│   └── ui/                  # Flutter 앱
├── docker-compose.yml       # 서비스 오케스트레이션
├── init.sql                 # DB 초기화 스크립트
└── .env.example             # 환경 변수 템플릿
```

## 🧪 테스트

### 단위 테스트 실행

```bash
# Agent (Python)
cd sourcecode/agent && pytest -v

# Indexer (Kotlin)
cd sourcecode/indexer && ./gradlew test

# Chatbot (Kotlin)
cd sourcecode/chatbot && ./gradlew test

# UI (Flutter)
cd sourcecode/ui && flutter test
```

### 통합 테스트 실행

```bash
./03_Test/scripts/integration_test.sh
```

## 📊 테스트 현황

| 컴포넌트 | 테스트 수 | 상태 |
|---------|---------|------|
| Agent | 15 | ✅ |
| Indexer | 15 | ✅ |
| Chatbot | 8 | ✅ |
| UI | 1 | ✅ |

## 🔧 개발 환경 설정

### Agent (Python)

```bash
cd sourcecode/agent
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### Chatbot/Indexer (Kotlin)

```bash
cd sourcecode/chatbot  # 또는 indexer
./gradlew run
```

### UI (Flutter)

```bash
cd sourcecode/ui
flutter pub get
flutter run
```

## 📝 문서

- [기능 요구사항](./01_Requirement/01_functional_requirements.md)
- [시스템 아키텍처](./02_Design/01_system_architecture.md)
- [API 설계](./02_Design/02_api_design.md)
- [테스트 계획](./03_Test/01_test_plan.md)
- [통합 테스트 계획](./03_Test/02_integration_test_plan.md)

## 📄 라이선스

MIT License
