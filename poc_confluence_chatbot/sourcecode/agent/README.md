# Agent Service

Python 기반 RAG (Retrieval-Augmented Generation) 서비스

## 기술 스택
- Python 3.12
- FastAPI
- LangChain
- sentence-transformers
- PostgreSQL + pgvector

## 실행 방법

```bash
# 가상환경 생성
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 개발 서버 실행
uvicorn app.main:app --reload --port 8000

# 테스트 실행
pytest -v
```

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/embed` | 문서 임베딩 및 저장 |
| DELETE | `/embed/{page_id}` | 임베딩 삭제 |
| POST | `/query` | RAG 기반 질의응답 |
| GET | `/health` | 헬스체크 |

## 환경 변수

```bash
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/confluence_chatbot
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=gpt-oss:20b
```
