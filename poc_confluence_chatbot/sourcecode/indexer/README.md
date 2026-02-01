# Indexer Service

Kotlin + Vert.x 기반 Confluence 문서 색인 서비스

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

## 환경 변수

```bash
CONFLUENCE_BASE_URL=https://your-domain.atlassian.net
CONFLUENCE_API_TOKEN=your-api-token
CONFLUENCE_USER_EMAIL=your-email@example.com
CONFLUENCE_SPACE_KEY=DEV
AGENT_BASE_URL=http://localhost:8000
```
