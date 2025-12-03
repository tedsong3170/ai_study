# LangChain4j Error Code API

LangChain4j를 활용하여 로컬 LLM 기반의 에러코드 설명 API를 제공하는 Spring Boot 애플리케이션입니다.

## 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot 3.2.1
- **JDK**: 21
- **Build Tool**: Gradle 8.5
- **AI Framework**: LangChain4j 0.34.0
- **LLM**: Ollama (Docker)

## 빠른 시작 가이드

프로젝트를 실행하려면:

1. **Ollama 시작:**
   ```bash
   # Ollama 서비스 시작 (백그라운드)
   brew services start ollama
   
   # 모델 다운로드 (최초 1회)
   ollama pull llama2
   ```

2. **애플리케이션 실행:**
   ```bash
   cd /Users/song/dev/LangChain_01
   ./gradlew bootRun
   ```

## 프로젝트 구조

```
LangChain_01/
├── docker-compose.yml              # Ollama Docker 설정
├── build.gradle.kts                # Gradle 빌드 설정
├── OLLAMA_SETUP.md                 # Ollama 설치 가이드
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/langchain/
│   │   │       ├── LangChainApplication.kt
│   │   │       ├── config/           # LangChain4j 설정
│   │   │       ├── domain/           # 도메인 모델
│   │   │       ├── repository/       # 데이터 저장소
│   │   │       ├── service/          # 비즈니스 로직
│   │   │       └── controller/       # REST API
│   │   └── resources/
│   │       └── application.yml       # 애플리케이션 설정
│   └── test/
│       └── kotlin/                   # 테스트 코드
└── tdd.md                            # TDD 가이드
```

## 시작하기

### 사전 요구사항

- JDK 21
- Ollama (로컬 설치)

### 1. Ollama 설치 및 실행

```bash
# Ollama 설치 (macOS)
brew install ollama

# Ollama 서비스 시작 (백그라운드)
brew services start ollama

# 또는 포그라운드 실행
# ollama serve

# llama2 모델 다운로드 (약 3.8GB - 최초 1회만 필요)
ollama pull llama2

# Ollama 정상 동작 확인
curl http://localhost:11434/api/tags
```

자세한 내용은 [OLLAMA_SETUP.md](OLLAMA_SETUP.md)를 참고하세요.

### 2. 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

## 개발 가이드

이 프로젝트는 **TDD(Test-Driven Development)** 방식을 따릅니다. 자세한 내용은 [tdd.md](tdd.md)를 참고하세요.

### 개발 단계

- [x] **Phase 0**: 프로젝트 초기 설정
- [ ] **Phase 1**: 도메인 모델 설계 (TDD)
- [ ] **Phase 2**: LangChain4j 통합 (TDD)
- [ ] **Phase 3**: API 엔드포인트 개발 (TDD)
- [ ] **Phase 4**: 검증 및 문서화

## API 엔드포인트 (예정)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/error-codes` | 모든 에러코드 조회 |
| GET | `/api/error-codes/{code}` | 특정 에러코드 조회 |
| POST | `/api/error-codes/query` | AI 질의응답 |

## 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.example.langchain.LangChainApplicationTests"
```

## 설정

`src/main/resources/application.yml`에서 설정을 변경할 수 있습니다:

```yaml
langchain:
  ollama:
    base-url: http://localhost:11434  # Ollama 서버 주소
    model-name: llama2                # 사용할 모델 이름
    timeout: 60s                      # 타임아웃 설정
```

## 라이선스

이 프로젝트는 학습 목적으로 제작되었습니다.
