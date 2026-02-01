# 테스트 문서

Confluence Chatbot 시스템의 테스트 관련 문서입니다.

## 문서 목록

| 문서 | 설명 |
|-----|------|
| [테스트 계획서](./01_test_plan.md) | 테스트 전략, 케이스, 환경 정의 |
| [통합 테스트 계획서](./02_integration_test_plan.md) | Agent/Indexer/Chatbot 연동 테스트 |

## 테스트 현황 요약

| 컴포넌트 | 상태 | 비고 |
|---------|------|-----|
| Agent (Python) | ✅ 완료 | 15/15 passed |
| Indexer (Kotlin) | ✅ 완료 | BUILD SUCCESSFUL |
| Chatbot (Kotlin) | ⏳ 대기 | 개발 예정 |
| UI (Flutter) | ⏳ 대기 | 개발 예정 |
| 통합 테스트 | ⏳ 대기 | 전체 컴포넌트 완료 후 |

## 테스트 실행 명령어

```bash
# Agent 테스트
cd sourcecode/agent && source venv/bin/activate && pytest -v

# Indexer 테스트  
cd sourcecode/indexer && ./gradlew test

# Chatbot 테스트
cd sourcecode/chatbot && ./gradlew test

# UI 테스트
cd sourcecode/ui && flutter test
```
