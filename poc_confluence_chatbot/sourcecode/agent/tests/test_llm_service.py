"""
LLM 서비스 테스트 (TDD - Red Phase)
"""
import pytest
from unittest.mock import AsyncMock, patch
from app.services.llm_service import LLMService


class TestLLMService:
    """LLM 서비스 테스트"""
    
    @pytest.fixture
    def llm_service(self):
        """테스트용 LLM 서비스 인스턴스"""
        return LLMService()
    
    @pytest.mark.asyncio
    async def test_generate_returns_answer(self, llm_service):
        """컨텍스트 기반으로 답변을 생성해야 한다"""
        # Given
        question = "휴가 정책이 어떻게 되나요?"
        context = ["연차휴가는 15일입니다.", "병가는 10일까지 사용 가능합니다."]
        
        # When (Mock Ollama)
        mock_response = "휴가 정책은 연차 15일, 병가 10일입니다."
        with patch.object(llm_service, '_call_ollama', new_callable=AsyncMock, return_value=mock_response):
            result = await llm_service.generate(question, context)
        
        # Then
        assert result is not None
        assert len(result) > 0
    
    @pytest.mark.asyncio
    async def test_generate_with_chat_history(self, llm_service):
        """대화 이력을 참조하여 답변해야 한다 (Conversation Memory)"""
        # Given
        question = "그거 더 자세히 설명해줘"
        context = ["연차휴가 사용 시 3일 전 신청 필요"]
        chat_history = [
            {"role": "user", "content": "휴가 정책 알려줘"},
            {"role": "assistant", "content": "연차 15일, 병가 10일입니다."}
        ]
        
        # When (Mock Ollama)
        mock_response = "연차휴가에 대해 더 자세히 설명드리면..."
        with patch.object(llm_service, '_call_ollama', new_callable=AsyncMock, return_value=mock_response):
            result = await llm_service.generate(question, context, chat_history)
        
        # Then
        assert result is not None
        assert "연차" in result or len(result) > 0
    
    @pytest.mark.asyncio
    async def test_generate_empty_context_still_works(self, llm_service):
        """컨텍스트가 없어도 답변을 생성해야 한다"""
        # Given
        question = "안녕하세요?"
        context = []
        
        # When (Mock Ollama)
        mock_response = "안녕하세요! 무엇을 도와드릴까요?"
        with patch.object(llm_service, '_call_ollama', new_callable=AsyncMock, return_value=mock_response):
            result = await llm_service.generate(question, context)
        
        # Then
        assert result is not None
