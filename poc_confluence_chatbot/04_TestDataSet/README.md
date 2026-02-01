# 테스트 데이터셋

Confluence Chatbot 테스트를 위한 샘플 데이터 생성 도구입니다.

## 가상환경 설정

```bash
# 가상환경 생성
python3 -m venv venv

# 활성화 (Mac/Linux)
source venv/bin/activate

# 활성화 (Windows)
.\venv\Scripts\activate

# 의존성 설치
pip install requests
```

## 환경 변수 설정

스크립트 실행 전 환경 변수를 설정하세요:

```bash
# 프로젝트 루트의 .env 파일 사용
source ../.env

# 또는 직접 설정
export CONFLUENCE_BASE_URL="https://your-domain.atlassian.net"
export CONFLUENCE_USER_EMAIL="your-email@example.com"
export CONFLUENCE_API_TOKEN="your-api-token"
export CONFLUENCE_SPACE_KEY="your-space-key"
```

## 스크립트 실행

```bash
# 가상환경 활성화 후 실행
python create_confluence_data.py
```

## 생성되는 문서

| 문서 제목 | 설명 |
|----------|------|
| 휴가 정책 | 연차, 병가, 경조사 휴가 규정 |
| 출장 규정 | 국내/해외 출장비 지급 기준 |
| 보안 정책 | 비밀번호, 접근권한, 사고보고 |
| 신입사원 온보딩 가이드 | 첫 주 일정, 필수교육, 멘토링 |
| 회의실 예약 규정 | 예약방법, 회의실 종류 |

## 주의사항

- Confluence API 토큰은 [여기](https://id.atlassian.com/manage-profile/security/api-tokens)에서 생성
- Space Key는 Confluence Space 설정에서 확인
- 동일 제목의 문서가 이미 있으면 에러 발생
