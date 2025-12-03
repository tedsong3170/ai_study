# Ollama 로컬 설치 가이드 (macOS)

## Quick Start

### 1. Ollama 설치
```bash
# Homebrew를 통한 설치 (권장)
brew install ollama

# 또는 공식 웹사이트에서 다운로드
# https://ollama.ai/download
```

### 2. Ollama 서비스 시작
```bash
# 백그라운드에서 Ollama 서버 시작
ollama serve
```

> **참고**: 터미널을 닫으면 서비스가 종료됩니다. 백그라운드에서 계속 실행하려면 별도 터미널을 열어두거나, 아래 "백그라운드 실행" 섹션을 참고하세요.

### 3. 모델 다운로드
```bash
# 새 터미널을 열고 실행

# llama2 모델 다운로드 (약 3.8GB)
ollama pull llama2

# 또는 더 가벼운 mistral 모델 (약 4.1GB)
ollama pull mistral

# 또는 더 작고 빠른 phi3 모델 (약 2.3GB)
ollama pull phi3
```

### 4. Ollama 상태 확인
```bash
# 설치된 모델 확인
ollama list

# API 동작 확인
curl http://localhost:11434/api/tags
```

### 5. 테스트 쿼리 실행
```bash
# 터미널에서 대화형으로 테스트
ollama run llama2

# 또는 API로 직접 호출
curl http://localhost:11434/api/generate -d '{
  "model": "llama2",
  "prompt": "Why is the sky blue?",
  "stream": false
}'
```

## 백그라운드 실행

### 방법 1: nohup 사용
```bash
nohup ollama serve > /dev/null 2>&1 &
```

### 방법 2: Homebrew 서비스로 등록 (권장)
```bash
# 서비스 시작 (시스템 부팅 시 자동 시작)
brew services start ollama

# 서비스 중지
brew services stop ollama

# 서비스 재시작
brew services restart ollama

# 서비스 상태 확인
brew services list | grep ollama
```

## 관리 명령어

### 모델 관리
```bash
# 설치된 모델 목록
ollama list

# 모델 삭제
ollama rm llama2

# 모델 업데이트
ollama pull llama2
```

### 서비스 관리
```bash
# Ollama 프로세스 확인
ps aux | grep ollama

# Ollama 프로세스 종료
pkill ollama
```

## 성능 팁

- **CPU vs GPU**: Apple Silicon (M1/M2/M3) Mac의 경우 Metal을 자동으로 활용하여 빠른 추론 속도 제공
- **모델 선택**: 
  - 빠른 응답이 필요하면: `phi3` (2.3GB)
  - 균형잡힌 성능: `llama2` (3.8GB)
  - 더 나은 품질: `llama2:13b` (7.3GB) 또는 `mistral` (4.1GB)
- **메모리**: 최소 8GB RAM 권장, 13B 모델 사용 시 16GB 이상 권장

## 사용 가능한 모델

| 모델 | 크기 | 특징 |
|------|------|------|
| `phi3` | 2.3GB | 빠르고 가벼움, 일반적인 작업에 적합 |
| `llama2` | 3.8GB | 균형잡힌 성능, 범용 용도 |
| `mistral` | 4.1GB | 고품질 응답, 코딩 작업 우수 |
| `codellama` | 3.8GB | 코드 생성 특화 |
| `llama2:13b` | 7.3GB | 더 큰 모델, 더 나은 품질 |

더 많은 모델은 https://ollama.ai/library 에서 확인할 수 있습니다.

## 문제 해결

### 포트가 이미 사용 중인 경우
```bash
# 포트 11434를 사용하는 프로세스 확인
lsof -i :11434

# 해당 프로세스 종료 후 재시작
pkill ollama
ollama serve
```

### 모델 다운로드 실패
```bash
# 캐시 삭제 후 재시도
rm -rf ~/.ollama/models
ollama pull llama2
```
