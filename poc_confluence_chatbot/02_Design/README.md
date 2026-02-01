# 02_Design - 시스템 설계 문서

## 📋 문서 목록

| 파일 | 설명 | 상태 |
|-----|------|------|
| [01_system_architecture.md](./01_system_architecture.md) | 시스템 아키텍처 설계 | ✅ 초안 완료 |
| [02_api_design.md](./02_api_design.md) | API 설계 | ✅ 초안 완료 |
| [03_database_design.md](./03_database_design.md) | 데이터베이스 설계 | ✅ 초안 완료 |
| [04_interface_design.md](./04_interface_design.md) | 인터페이스 설계 | ✅ 초안 완료 |

---

## 🏗️ 시스템 구성 요약

```
┌─────────────────────────────────────────────────────────────┐
│                     로컬 개발 환경                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌──────────────┐                                          │
│   │  Flutter UI  │ ◄─── macOS / Android                     │
│   │   (Client)   │                                          │
│   └──────┬───────┘                                          │
│          │ REST API                                         │
│          ▼                                                   │
│   ┌──────────────┐     ┌──────────────┐                     │
│   │   Chatbot    │     │   Indexer    │                     │
│   │  (Vert.x)    │     │  (Vert.x)    │◄── Confluence API   │
│   │   :8080      │     │   :8081      │                     │
│   └──────┬───────┘     └──────┬───────┘                     │
│          │                    │                              │
│          └────────┬───────────┘                             │
│                   │ HTTP API                                 │
│                   ▼                                          │
│          ┌──────────────┐                                   │
│          │    Agent     │                                   │
│          │ (LangChain)  │                                   │
│          │   :8000      │                                   │
│          └──────┬───────┘                                   │
│                 │                                            │
│       ┌─────────┴─────────┐                                 │
│       ▼                   ▼                                  │
│ ┌──────────────┐   ┌──────────────┐                         │
│ │  PostgreSQL  │   │   Ollama     │                         │
│ │  + pgvector  │   │ gpt-oss:20b  │                         │
│ │   :5432      │   │   :11434     │                         │
│ └──────────────┘   └──────────────┘                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 기술 스택

| 컴포넌트 | 기술 | 버전 |
|---------|------|------|
| **Indexer** | Kotlin + Vert.x | 1.9 / 4.5.24 |
| **Chatbot** | Kotlin + Vert.x | 1.9 / 4.5.24 |
| **Agent** | Python + LangChain + FastAPI | 3.12 |
| **UI** | Flutter | latest |
| **Database** | PostgreSQL + pgvector | 15+ |
| **LLM** | Ollama (gpt-oss:20b) | latest |

---

## 📡 API 엔드포인트 요약

### Chatbot (:8080)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chat` | 질문 전송 |
| GET | `/api/chat/history` | 대화 이력 |
| GET | `/health` | 헬스체크 |

### Agent (:8000)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/embed` | 문서 임베딩 |
| DELETE | `/embed/{page_id}` | 임베딩 삭제 |
| POST | `/query` | RAG 질의 |
| GET | `/health` | 헬스체크 |

### Indexer (:8081)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/index/run` | 색인 실행 |
| GET | `/api/index/status` | 색인 상태 |
| GET | `/health` | 헬스체크 |

---

## 🗄️ 데이터베이스 테이블

| 테이블 | 설명 |
|-------|------|
| `document_chunks` | 문서 청크 및 벡터 임베딩 |
| `index_jobs` | 색인 작업 이력 |
| `conversations` | 대화 세션 |
| `messages` | 대화 메시지 |

---

## 🚀 배포 구성

Docker Compose를 사용한 로컬 배포:

```bash
# 1. Ollama 호스트 설치 및 실행 (필수 - Docker 성능 이슈로 호스트 설치 권장)
brew install ollama
ollama serve &
ollama pull gpt-oss:20b

# 2. Docker 서비스 시작 (PostgreSQL, Agent, Chatbot, Indexer)
docker-compose up -d

# 3. 로그 확인
docker-compose logs -f
```

> ⚠️ **주의**: Ollama는 Docker가 아닌 호스트에 설치해야 GPU 가속으로 빠른 응답이 가능합니다.

---

## 📅 문서 이력

| 날짜 | 버전 | 변경 내용 |
|-----|------|----------|
| 2026-01-19 | 0.1 | 시스템 설계 초안 작성 |
