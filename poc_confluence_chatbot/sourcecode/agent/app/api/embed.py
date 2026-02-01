"""
Embed API - 문서 임베딩 및 저장
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional

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


class EmbedRequest(BaseModel):
    """임베딩 요청"""
    page_id: str
    space_key: str
    title: str
    content: str
    category: str = ""
    page_url: Optional[str] = None


class EmbedResponse(BaseModel):
    """임베딩 응답"""
    status: str
    page_id: str
    chunks_created: int
    message: str


class DeleteResponse(BaseModel):
    """삭제 응답"""
    status: str
    page_id: str
    chunks_deleted: int


@router.post("/embed", response_model=EmbedResponse)
async def embed_document(request: EmbedRequest) -> EmbedResponse:
    """
    문서를 임베딩하여 벡터 DB에 저장
    
    Args:
        request: 임베딩할 문서 정보
        
    Returns:
        EmbedResponse: 임베딩 결과
    """
    try:
        rag_chain = get_rag_chain()
        chunks_created = await rag_chain.embed_document(
            page_id=request.page_id,
            space_key=request.space_key,
            title=request.title,
            content=request.content,
            category=request.category,
            page_url=request.page_url or ""
        )
        
        return EmbedResponse(
            status="success",
            page_id=request.page_id,
            chunks_created=chunks_created,
            message="Document embedded successfully"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/embed/{page_id}", response_model=DeleteResponse)
async def delete_embedding(page_id: str) -> DeleteResponse:
    """
    특정 페이지의 임베딩 삭제
    
    Args:
        page_id: Confluence 페이지 ID
        
    Returns:
        DeleteResponse: 삭제 결과
    """
    try:
        rag_chain = get_rag_chain()
        chunks_deleted = await rag_chain.delete_document(page_id)
        
        return DeleteResponse(
            status="success",
            page_id=page_id,
            chunks_deleted=chunks_deleted
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
