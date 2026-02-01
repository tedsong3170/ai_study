# AI Study Result: Confluence Chatbot POC

## 1. 개요 (Overview)
본 문서는 AI 스터디의 결과물로 진행된 'Confluence Chatbot POC' 프로젝트에 대한 정리입니다.
이 프로젝트는 RAG(Retrieval-Augmented Generation) 기술을 활용하여 Confluence 문서를 기반으로 자연어 질의응답이 가능한 시스템을 구축하는 것을 목표로 했습니다.

## 2. 주요 기능 및 특징
- **RAG 기반 검색**: 사내 Confluence 문서를 벡터화하여 저장하고, 질문과 관련된 문서를 검색하여 답변을 생성합니다.
- **대화 맥락 유지 (Conversation Memory)**: 이전 대화의 문맥을 기억하여 연속적인 질문과 답변이 가능합니다.
- **마이크로서비스 아키텍처**: 기능별로 서비스를 분리하여 확장성과 유지보수성을 고려했습니다.

## 3. 기술 스택 및 아키텍처
시스템은 다음과 같은 기술 스택과 구조로 설계되었습니다.

### 아키텍처 구성
- **UI (Flutter)**: 사용자 채팅 인터페이스 제공 (Web/Mobile 지원)
- **Chatbot (Kotlin/Vert.x)**: 대화 세션 관리 및 비즈니스 로직 처리, Conversation Memory 담당
- **Agent (Python/FastAPI)**: 핵심 AI 로직 수행 (문서 임베딩, 벡터 검색, LLM 호출)
- **Indexer (Kotlin/Vert.x)**: Confluence 문서 수집 및 색인 배치 작업
- **DB (PostgreSQL + pgvector)**: 문서 벡터 및 메타데이터 저장
- **LLM (Ollama)**: 로컬 환경에서 `gpt-oss:20b` 모델을 구동하여 추론 수행

### 데이터 흐름
1. 사용자가 질문을 입력 (UI -> Chatbot)
2. Chatbot이 대화 이력을 포함하여 Agent에 질의
3. Agent가 질문을 벡터화하고 DB에서 관련 문서 검색
4. 검색된 문서를 바탕으로 LLM이 답변 생성
5. 답변을 사용자에게 전달

## 4. 구현 결과
### API 엔드포인트
- **Chatbot API (:8080)**: `/api/chat`을 통해 질의응답 처리, `/api/chat/history`로 대화 이력 조회
- **Agent API (:8000)**: `/embed`로 문서 임베딩, `/query`로 RAG 기반 검색 수행

### 테스트 현황
각 컴포넌트별 단위 테스트 및 통합 테스트를 수행하여 안정성을 검증했습니다.
- Agent, Indexer, Chatbot, UI 각 영역별 테스트 케이스 작성 및 통과 확인

## 5. 프로젝트 구조
프로젝트는 요구사항 정의부터 설계, 구현, 테스트까지 체계적으로 관리되었습니다.
- `01_Requirement`: 기능/비기능 요구사항 정의
- `02_Design`: 시스템 아키텍처, API, DB 설계
- `03_Test`: 테스트 계획 및 통합 테스트 스크립트
- `sourcecode`: 각 서비스별 소스 코드 (Agent, Chatbot, Indexer, UI)

## 6. 결론
이번 스터디를 통해 RAG 아키텍처의 실제 구현과 LLM을 활용한 애플리케이션 개발 프로세스를 경험했습니다. 특히, 벡터 데이터베이스와 로컬 LLM을 연동하여 보안성을 고려한 문서 검색 시스템의 가능성을 확인했습니다.
