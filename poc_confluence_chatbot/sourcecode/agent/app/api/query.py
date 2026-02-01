"""
Query API - RAG 기반 질의응답
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List

from app.services.rag_chain import RAGChain

router = APIRouter()

# 글로벌 RAG 체인 인스턴스
_rag_chain: RAGChain = None


def get_rag_chain() -> RAGChain:
    """RAG 체인 싱글톤"""
    global _rag_chain
    if _rag_chain is None:
        _rag_chain = RAGChain()
    return _rag_chain


class ChatMessage(BaseModel):
    """대화 메시지 (Conversation Memory)"""
    role: str  # "user" | "assistant"
    content: str


class QueryRequest(BaseModel):
    """질의 요청"""
    question: str
    top_k: int = 10
    space_key: Optional[str] = None
    chat_history: Optional[List[ChatMessage]] = None


class Source(BaseModel):
    """출처 정보"""
    page_id: str
    title: str
    url: str
    chunk_content: str
    relevance_score: float


class QueryResponse(BaseModel):
    """질의 응답"""
    answer: str
    sources: List[Source]
    processing_time_ms: int


@router.post("/query", response_model=QueryResponse)
async def query_documents(request: QueryRequest) -> QueryResponse:
    """
    RAG 기반 질의응답
    
    1. 질문을 임베딩
    2. 벡터 DB에서 관련 문서 검색
    3. 대화 이력(chat_history)과 함께 LLM에 전달
    4. 답변 생성
    
    Args:
        request: 질의 요청 (질문, 대화 이력 포함)
        
    Returns:
        QueryResponse: 답변 및 출처
    """
    try:
        rag_chain = get_rag_chain()
        
        # chat_history를 dict 형태로 변환
        chat_history = None
        if request.chat_history:
            chat_history = [
                {"role": msg.role, "content": msg.content}
                for msg in request.chat_history
            ]
        
        result = await rag_chain.query(
            question=request.question,
            top_k=request.top_k,
            space_key=request.space_key,
            chat_history=chat_history
        )
        
        # SearchResult를 Source로 변환
        sources = [
            Source(
                page_id=src.page_id,
                title=src.title,
                url=src.url,
                chunk_content=src.content,
                relevance_score=src.score
            )
            for src in result.sources
        ]
        
        return QueryResponse(
            answer=result.answer,
            sources=sources,
            processing_time_ms=result.processing_time_ms
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
