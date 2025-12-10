# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Methodology

Follow TDD (Test-Driven Development) with Tidy First principles as defined in tdd.md:
- Red → Green → Refactor cycle
- When user says "go": find next unmarked test in plan.md, implement test, then implement minimum code to pass
- Separate STRUCTURAL changes (refactoring) from BEHAVIORAL changes (new functionality) in commits
- Always structural changes first, then behavioral

## Build Commands

```bash
# Build (once project is initialized)
./gradlew build

# Run tests
./gradlew test

# Run single test
./gradlew test --tests "TestClassName.testMethodName"

# Run application
./gradlew bootRun
```

## Tech Stack

- **Framework**: Spring Boot 3
- **AI/LLM**: LangChain4j with Ollama
- **Language**: Java

## Project Purpose

자연어 질의 기반 주식 정보 조회 서비스:
- 사용자가 자연어로 주식 정보를 질의하면 LangChain4j + Ollama가 의도를 파악
- KIS API를 호출하여 실시간 데이터를 가져옴
- 자연어로 응답 제공

## 주요 기능

KIS API를 통해 제공하는 실시간 정보:
- 가격 급등락 종목 조회
- 거래량 급등락 종목 조회
- 분봉 데이터 조회
- 종목 상세 데이터 조회

## Architecture

```
[사용자 자연어 질의]
    → [LangChain4j + Ollama: 의도 파악 및 파라미터 추출]
    → [KIS API Client: 실시간 데이터 조회]
    → [LangChain4j + Ollama: 자연어 응답 생성]
    → [사용자에게 응답]
```

### 핵심 컴포넌트
- **ChatController**: REST API 엔드포인트 (POST /api/chat)
- **ChatService**: 사용자 질의 처리 및 응답 생성
- **KisApiClient**: KIS Open API 호출
- **StockTools**: LangChain4j Tool로 등록된 주식 조회 기능들

## API Endpoint

```
POST /api/chat
Content-Type: application/json

Request:
{
  "message": "오늘 가격 급등한 종목 알려줘"
}

Response:
{
  "response": "오늘 가격이 급등한 종목은 다음과 같습니다: ..."
}
```

## Architecture Notes

Project follows plan.md for incremental TDD development. Each test case in plan.md should be implemented one at a time.