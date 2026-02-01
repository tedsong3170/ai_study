# Chatbot Service

Kotlin + Vert.x 기반 채팅 API 서비스

## 기술 스택
- Kotlin 1.9
- Vert.x 4.5.x
- Gradle (Kotlin DSL)

## 실행 방법

```bash
# 빌드
./gradlew build

# 실행
./gradlew run

# 테스트
./gradlew test
```

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chat` | 질문 전송 및 답변 수신 |
| GET | `/api/chat/history` | 대화 이력 조회 |
| GET | `/health` | 헬스체크 |

## 환경 변수

```bash
AGENT_BASE_URL=http://localhost:8000
CHATBOT_PORT=8080
```
