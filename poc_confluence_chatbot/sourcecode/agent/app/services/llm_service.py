"""
LLM 서비스 (TDD - Green Phase)
Ollama를 사용한 답변 생성 (Conversation Memory 지원)
"""
from typing import List, Optional, Dict
import httpx

from app.config import get_settings


class LLMService:
    """LLM 서비스 - Ollama 연동"""
    
    def __init__(self, base_url: str = None, model: str = None):
        """
        Args:
            base_url: Ollama API URL
            model: 사용할 모델명
        """
        settings = get_settings()
        self._base_url = base_url or settings.ollama_base_url
        self._model = model or settings.ollama_model
        self._timeout = settings.ollama_timeout
        self._max_history_turns = settings.max_chat_history_turns
    
    async def generate(
        self,
        question: str,
        context: List[str],
        chat_history: Optional[List[Dict[str, str]]] = None,
        max_tokens: int = 1024
    ) -> str:
        """
        컨텍스트 및 대화 이력 기반 답변 생성
        
        Args:
            question: 사용자 질문
            context: RAG로 검색된 문서 청크들
            chat_history: 이전 대화 이력 [{"role": "user"|"assistant", "content": "..."}]
            max_tokens: 최대 토큰 수
            
        Returns:
            str: 생성된 답변
        """
        prompt = self._build_prompt(question, context, chat_history)
        return await self._call_ollama(prompt, max_tokens)
    
    def _build_prompt(
        self,
        question: str,
        context: List[str],
        chat_history: Optional[List[Dict[str, str]]] = None
    ) -> str:
        """
        RAG 프롬프트 구성
        
        Args:
            question: 사용자 질문
            context: 검색된 문서 컨텍스트
            chat_history: 대화 이력
            
        Returns:
            str: 완성된 프롬프트
        """
        prompt_parts = []
        
        # 시스템 프롬프트
        prompt_parts.append(
            "당신은 Confluence 문서를 기반으로 질문에 답변하는 AI 어시스턴트입니다.\n"
            "제공된 문서 컨텍스트를 기반으로 정확하게 답변해주세요.\n"
            "문서에 없는 내용은 '문서에서 해당 정보를 찾을 수 없습니다'라고 답변해주세요.\n"
        )
        
        # 문서 컨텍스트
        if context:
            prompt_parts.append("\n### 참고 문서:\n")
            for i, doc in enumerate(context, 1):
                prompt_parts.append(f"[문서 {i}]\n{doc}\n")
        
        # 대화 이력 (Conversation Memory)
        if chat_history:
            # 최근 N턴만 사용
            recent_history = chat_history[-(self._max_history_turns * 2):]
            
            prompt_parts.append("\n### 이전 대화:\n")
            for msg in recent_history:
                role = "사용자" if msg["role"] == "user" else "어시스턴트"
                prompt_parts.append(f"{role}: {msg['content']}\n")
        
        # 현재 질문
        prompt_parts.append(f"\n### 현재 질문:\n{question}\n")
        prompt_parts.append("\n### 답변:\n")
        
        return "".join(prompt_parts)
    
    async def _call_ollama(self, prompt: str, max_tokens: int = 1024) -> str:
        """
        Ollama API 호출
        
        Args:
            prompt: 전체 프롬프트
            max_tokens: 최대 토큰 수
            
        Returns:
            str: 생성된 텍스트
        """
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            response = await client.post(
                f"{self._base_url}/api/generate",
                json={
                    "model": self._model,
                    "prompt": prompt,
                    "stream": False,
                    "options": {
                        "num_predict": max_tokens
                    }
                }
            )
            response.raise_for_status()
            
            result = response.json()
            return result.get("response", "")
