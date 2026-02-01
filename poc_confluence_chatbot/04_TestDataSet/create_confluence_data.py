#!/usr/bin/env python3
"""
Confluence 테스트 데이터 생성 스크립트
회사 정책, 규정 등 샘플 문서를 Confluence에 생성합니다.
"""

import os
import requests
from requests.auth import HTTPBasicAuth
import json
from pathlib import Path

# .env 파일 직접 로드
def load_env_file():
    """상위 디렉토리의 .env 파일을 읽어 환경변수로 설정"""
    env_path = Path(__file__).parent.parent / ".env"
    if env_path.exists():
        with open(env_path, 'r') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    key = key.replace('export ', '').strip()
                    value = value.strip().strip('"').strip("'")
                    os.environ[key] = value
        print(f"✅ .env 파일 로드: {env_path}")
    else:
        print(f"⚠️ .env 파일 없음: {env_path}")

load_env_file()

# 환경 변수에서 설정 로드
CONFLUENCE_BASE_URL = os.getenv("CONFLUENCE_BASE_URL", "")
CONFLUENCE_USER_EMAIL = os.getenv("CONFLUENCE_USER_EMAIL", "")
CONFLUENCE_API_TOKEN = os.getenv("CONFLUENCE_API_TOKEN", "")
CONFLUENCE_SPACE_KEY = os.getenv("CONFLUENCE_SPACE_KEY", "")

# API 설정
API_URL = f"{CONFLUENCE_BASE_URL}/wiki/rest/api/content"
auth = HTTPBasicAuth(CONFLUENCE_USER_EMAIL, CONFLUENCE_API_TOKEN)
headers = {
    "Accept": "application/json",
    "Content-Type": "application/json"
}

# 테스트 문서 데이터
TEST_DOCUMENTS = [
    {
        "title": "휴가 정책",
        "content": """
<h2>연차휴가</h2>
<p>연차휴가는 근속연수에 따라 다음과 같이 부여됩니다:</p>
<ul>
<li>1년 미만: 11일</li>
<li>1년 이상 3년 미만: 15일</li>
<li>3년 이상 5년 미만: 17일</li>
<li>5년 이상: 20일</li>
</ul>

<h2>병가</h2>
<p>병가는 연간 10일까지 유급으로 사용 가능합니다. 3일 이상 연속 사용 시 진단서 제출이 필요합니다.</p>

<h2>경조사 휴가</h2>
<ul>
<li>본인 결혼: 5일</li>
<li>자녀 결혼: 1일</li>
<li>배우자 출산: 10일</li>
<li>부모/배우자 사망: 5일</li>
<li>조부모/형제자매 사망: 3일</li>
</ul>

<h2>휴가 신청 절차</h2>
<p>휴가는 최소 3일 전에 사내 시스템을 통해 신청해야 합니다. 긴급한 경우 팀장에게 직접 연락 후 사후 신청이 가능합니다.</p>
"""
    },
    {
        "title": "출장 규정",
        "content": """
<h2>출장비 지급 기준</h2>

<h3>국내 출장</h3>
<ul>
<li>일비: 1일 3만원</li>
<li>숙박비: 1박 10만원 한도 (실비 정산)</li>
<li>식비: 1일 3만원</li>
<li>교통비: 실비 정산 (대중교통 기준)</li>
</ul>

<h3>해외 출장</h3>
<ul>
<li>일비: 지역에 따라 50~100달러</li>
<li>숙박비: 지역에 따라 150~250달러 한도</li>
<li>식비: 일비에 포함</li>
<li>항공: 이코노미 클래스 기본, 12시간 이상 비행 시 비즈니스 클래스 가능</li>
</ul>

<h2>출장 신청 절차</h2>
<ol>
<li>출장 최소 5일 전 사내 시스템에서 출장 신청</li>
<li>팀장 승인</li>
<li>출장 후 7일 이내 정산 보고서 제출</li>
</ol>

<h2>주의사항</h2>
<p>개인 용도 경비는 정산 불가합니다. 영수증은 반드시 보관해주세요.</p>
"""
    },
    {
        "title": "보안 정책",
        "content": """
<h2>비밀번호 정책</h2>
<ul>
<li>최소 12자 이상</li>
<li>대문자, 소문자, 숫자, 특수문자 각 1개 이상 포함</li>
<li>90일마다 변경 필수</li>
<li>최근 5개 비밀번호 재사용 불가</li>
</ul>

<h2>접근 권한</h2>
<p>모든 시스템 접근은 최소 권한 원칙(Least Privilege)에 따라 부여됩니다. 불필요한 권한은 즉시 반납해야 합니다.</p>

<h2>외부 반출 금지</h2>
<p>회사 기밀 정보는 외부로 반출할 수 없습니다. 업무상 필요한 경우 보안팀 승인 후 암호화하여 전송해야 합니다.</p>

<h2>사고 보고</h2>
<p>보안 사고 발생 시 즉시 보안팀(security@company.com)에 보고해야 합니다. 보고 지연 시 징계 대상이 될 수 있습니다.</p>
"""
    },
    {
        "title": "신입사원 온보딩 가이드",
        "content": """
<h2>첫 주 일정</h2>
<ul>
<li>1일차: HR 오리엔테이션, 계정 발급, 장비 수령</li>
<li>2일차: 회사 소개 및 조직 구조 설명</li>
<li>3일차: 팀 미팅 및 멘토 배정</li>
<li>4일차: 업무 도구 교육 (Jira, Confluence, Slack)</li>
<li>5일차: 팀 프로젝트 소개 및 첫 태스크 할당</li>
</ul>

<h2>필수 교육</h2>
<p>입사 후 1개월 이내에 다음 교육을 이수해야 합니다:</p>
<ul>
<li>보안 교육 (온라인, 2시간)</li>
<li>성희롱 예방 교육 (온라인, 1시간)</li>
<li>개인정보보호 교육 (온라인, 1시간)</li>
</ul>

<h2>멘토링 프로그램</h2>
<p>입사 후 3개월간 멘토가 배정됩니다. 멘토와 주 1회 이상 1:1 미팅을 진행하세요.</p>

<h2>수습 평가</h2>
<p>입사 3개월 후 수습 평가가 진행됩니다. 평가 결과에 따라 정규직 전환이 결정됩니다.</p>
"""
    },
    {
        "title": "회의실 예약 규정",
        "content": """
<h2>예약 방법</h2>
<p>회의실은 사내 캘린더 시스템을 통해 예약합니다. 예약은 선착순으로 진행됩니다.</p>

<h2>예약 규칙</h2>
<ul>
<li>최대 예약 가능 시간: 2시간</li>
<li>연속 예약: 불가 (최소 30분 간격 필요)</li>
<li>사전 예약: 최대 2주 전까지</li>
<li>노쇼(No-show) 3회 시 1주일간 예약 제한</li>
</ul>

<h2>회의실 종류</h2>
<ul>
<li>소회의실 (4인): 1층 101, 102호</li>
<li>중회의실 (8인): 2층 201, 202호</li>
<li>대회의실 (20인): 3층 301호</li>
<li>화상회의실: 각 층 VR실</li>
</ul>

<h2>사용 수칙</h2>
<p>회의 종료 후 의자를 정리하고, 화이트보드를 지워주세요. 음식물은 반입 금지입니다.</p>
"""
    }
]


def create_page(title: str, content: str) -> dict:
    """Confluence 페이지 생성"""
    
    payload = {
        "type": "page",
        "title": title,
        "space": {"key": CONFLUENCE_SPACE_KEY},
        "body": {
            "storage": {
                "value": content,
                "representation": "storage"
            }
        }
    }
    
    response = requests.post(
        API_URL,
        headers=headers,
        auth=auth,
        data=json.dumps(payload)
    )
    
    if response.status_code == 200:
        result = response.json()
        print(f"✅ 생성 완료: {title} (ID: {result['id']})")
        return result
    else:
        print(f"❌ 생성 실패: {title}")
        print(f"   상태 코드: {response.status_code}")
        print(f"   응답: {response.text}")
        return None


def main():
    """메인 함수"""
    
    print("=" * 50)
    print("Confluence 테스트 데이터 생성")
    print("=" * 50)
    print(f"URL: {CONFLUENCE_BASE_URL}")
    print(f"Space: {CONFLUENCE_SPACE_KEY}")
    print(f"문서 수: {len(TEST_DOCUMENTS)}")
    print("=" * 50)
    
    if not CONFLUENCE_API_TOKEN:
        print("❌ CONFLUENCE_API_TOKEN 환경 변수가 설정되지 않았습니다.")
        print("   다음 명령어로 설정하세요:")
        print("   export CONFLUENCE_API_TOKEN='your-api-token'")
        return
    
    created = 0
    failed = 0
    
    for doc in TEST_DOCUMENTS:
        result = create_page(doc["title"], doc["content"])
        if result:
            created += 1
        else:
            failed += 1
    
    print("=" * 50)
    print(f"결과: 성공 {created}, 실패 {failed}")
    print("=" * 50)


if __name__ == "__main__":
    main()
