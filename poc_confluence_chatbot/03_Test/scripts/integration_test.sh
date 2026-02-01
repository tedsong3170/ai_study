#!/bin/bash

# 통합 테스트 스크립트
# Confluence Chatbot - Agent, Indexer, Chatbot 통합 테스트

set -e

echo "======================================"
echo "Confluence Chatbot 통합 테스트"
echo "======================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

AGENT_URL=${AGENT_URL:-"http://localhost:8000"}
CHATBOT_URL=${CHATBOT_URL:-"http://localhost:8080"}

PASSED=0
FAILED=0

# 테스트 함수
test_case() {
    local name=$1
    local result=$2
    
    if [ "$result" -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $name"
        ((PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: $name"
        ((FAILED++))
    fi
}

# ===== 1. Agent 헬스체크 =====
echo ""
echo "📋 1. Agent 서비스 테스트"
echo "--------------------------------------"

response=$(curl -s -o /dev/null -w "%{http_code}" "$AGENT_URL/health")
test_case "INT-IDX-001: Agent 헬스체크" $([ "$response" == "200" ] && echo 0 || echo 1)

# ===== 2. 문서 임베딩 =====
echo ""
echo "📋 2. 문서 색인 테스트"
echo "--------------------------------------"

# 테스트 문서 임베딩
embed_response=$(curl -s -X POST "$AGENT_URL/embed" \
  -H "Content-Type: application/json" \
  -d '{
    "page_id": "INT-TEST-001",
    "space_key": "TEST",
    "title": "휴가 정책",
    "content": "연차휴가는 15일입니다. 병가는 10일입니다. 경조사 휴가는 별도로 지급됩니다.",
    "page_url": "http://test.confluence.com/wiki/1"
  }')

chunks=$(echo $embed_response | grep -o '"chunks_created":[0-9]*' | cut -d':' -f2)
test_case "INT-IDX-002: 문서 임베딩 (chunks=$chunks)" $([ "$chunks" -gt 0 ] 2>/dev/null && echo 0 || echo 1)

# 두 번째 문서 임베딩
embed_response2=$(curl -s -X POST "$AGENT_URL/embed" \
  -H "Content-Type: application/json" \
  -d '{
    "page_id": "INT-TEST-002",
    "space_key": "TEST",
    "title": "출장 규정",
    "content": "국내 출장 일비는 10만원입니다. 해외 출장은 지역에 따라 다릅니다.",
    "page_url": "http://test.confluence.com/wiki/2"
  }')

chunks2=$(echo $embed_response2 | grep -o '"chunks_created":[0-9]*' | cut -d':' -f2)
test_case "INT-IDX-003: 두 번째 문서 임베딩" $([ "$chunks2" -gt 0 ] 2>/dev/null && echo 0 || echo 1)

# ===== 3. Chatbot 헬스체크 =====
echo ""
echo "📋 3. Chatbot 서비스 테스트"
echo "--------------------------------------"

chatbot_health=$(curl -s -o /dev/null -w "%{http_code}" "$CHATBOT_URL/health")
test_case "INT-QA-001: Chatbot 헬스체크" $([ "$chatbot_health" == "200" ] && echo 0 || echo 1)

# ===== 4. 질의응답 테스트 =====
echo ""
echo "📋 4. 질의응답 테스트"
echo "--------------------------------------"

# 첫 번째 질문
chat_response=$(curl -s -X POST "$CHATBOT_URL/api/chat" \
  -H "Content-Type: application/json" \
  -d '{"question": "휴가 정책 알려줘"}')

conversation_id=$(echo $chat_response | grep -o '"conversation_id":"[^"]*"' | cut -d'"' -f4)
answer=$(echo $chat_response | grep -o '"answer":"[^"]*"' | head -1)

test_case "INT-QA-002: 첫 질문 응답" $([ -n "$conversation_id" ] && echo 0 || echo 1)
test_case "INT-QA-003: conversation_id 생성" $([ -n "$conversation_id" ] && echo 0 || echo 1)

echo "  → 대화 ID: $conversation_id"
echo "  → 답변 미리보기: ${answer:0:80}..."

# ===== 5. Conversation Memory 테스트 =====
echo ""
echo "📋 5. Conversation Memory 테스트 ⭐"
echo "--------------------------------------"

# 후속 질문 (맥락 참조)
followup_response=$(curl -s -X POST "$CHATBOT_URL/api/chat" \
  -H "Content-Type: application/json" \
  -d "{\"question\": \"그거 더 자세히 설명해줘\", \"conversation_id\": \"$conversation_id\"}")

followup_answer=$(echo $followup_response | grep -o '"answer":"[^"]*"' | head -1)
test_case "INT-MEM-001: 후속 질문 응답" $([ -n "$followup_answer" ] && echo 0 || echo 1)

# 대화 이력 조회
history_response=$(curl -s "$CHATBOT_URL/api/chat/history?conversation_id=$conversation_id")
message_count=$(echo $history_response | grep -o '"role"' | wc -l)

test_case "INT-MEM-002: 대화 이력 저장 (messages=$message_count)" $([ "$message_count" -ge 4 ] 2>/dev/null && echo 0 || echo 1)

# ===== 6. 문서 삭제 테스트 =====
echo ""
echo "📋 6. 문서 삭제 테스트"
echo "--------------------------------------"

delete_response=$(curl -s -X DELETE "$AGENT_URL/embed/INT-TEST-001")
delete_status=$(echo $delete_response | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
test_case "INT-IDX-004: 문서 삭제" $([ "$delete_status" == "success" ] && echo 0 || echo 1)

# 두 번째 문서도 삭제
curl -s -X DELETE "$AGENT_URL/embed/INT-TEST-002" > /dev/null

# ===== 결과 요약 =====
echo ""
echo "======================================"
echo "테스트 결과"
echo "======================================"
echo -e "통과: ${GREEN}$PASSED${NC}"
echo -e "실패: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 모든 테스트 통과!${NC}"
    exit 0
else
    echo -e "${RED}⚠️ 일부 테스트 실패${NC}"
    exit 1
fi
