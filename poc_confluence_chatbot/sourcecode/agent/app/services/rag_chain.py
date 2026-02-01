"""
RAG 체인 서비스 (Retrieval-Augmented Generation)
"""
import time
from dataclasses import dataclass
from typing import List, Optional, Dict

from app.services.embedding_service import EmbeddingService
from app.services.vector_store import VectorStore, SearchResult
from app.services.llm_service import LLMService
from app.config import get_settings


@dataclass
class RAGResult:
    """RAG 처리 결과"""
    answer: str
    sources: List[SearchResult]
    processing_time_ms: int


class RAGChain:
    """RAG 파이프라인"""
    
    def __init__(
        self,
        embedding_service: EmbeddingService = None,
        vector_store: VectorStore = None,
        llm_service: LLMService = None
    ):
        """
        Args:
            embedding_service: 임베딩 서비스
            vector_store: 벡터 스토어
            llm_service: LLM 서비스
        """
        self._embedding_service = embedding_service or EmbeddingService()
        self._vector_store = vector_store or VectorStore()
        self._llm_service = llm_service or LLMService()
    
    async def query(
        self,
        question: str,
        top_k: int = 5,
        space_key: Optional[str] = None,
        chat_history: Optional[List[Dict[str, str]]] = None
    ) -> RAGResult:
        """
        RAG 기반 질의응답
        
        1. 질문을 임베딩
        2. 벡터 DB에서 관련 문서 검색
        3. 대화 이력(chat_history)과 함께 LLM에 전달
        4. 답변 생성
        
        Args:
            question: 사용자 질문
            top_k: 검색할 문서 수
            space_key: 검색 제한할 Space
            chat_history: 대화 이력 (Conversation Memory)
            
        Returns:
            RAGResult: 답변, 출처, 처리 시간
        """
        start_time = time.time()
        
        # 1. 질문 임베딩
        print(f"DEBUG: Query text: {question}")
        query_embedding = self._embedding_service.embed(question)
        print(f"DEBUG: Query Vector (first 5): {query_embedding[:5]}")
        
        # 2. 벡터 검색 (제목 키워드 가중치 적용)
        search_results = await self._vector_store.search(
            query_embedding=query_embedding,
            top_k=top_k,
            space_key=space_key,
            query_text=question  # 제목 매칭 가중치 계산용
        )
        
        # 3. 컨텍스트 구성
        context = [result.content for result in search_results]
        
        # 4. LLM 답변 생성 (대화 이력 포함)
        answer = await self._llm_service.generate(
            question=question,
            context=context,
            chat_history=chat_history
        )
        
        processing_time_ms = int((time.time() - start_time) * 1000)
        
        return RAGResult(
            answer=answer,
            sources=search_results,
            processing_time_ms=processing_time_ms
        )
    
    async def embed_document(
        self,
        page_id: str,
        space_key: str,
        title: str,
        content: str,
        category: str = "",
        page_url: str = ""
    ) -> int:
        """
        문서를 청킹하고 임베딩하여 저장
        
        Args:
            page_id: 페이지 ID
            space_key: Space 키
            title: 문서 제목
            content: 문서 내용
            category: 카테고리 (Labels)
            page_url: 페이지 URL
            
        Returns:
            int: 저장된 청크 수
        """
        from app.services.chunking_service import ChunkingService
        
        chunking_service = ChunkingService()
        
        # 1. 텍스트를 청크로 분할
        chunks = chunking_service.split(content)
        
        if not chunks:
            return 0
        
        # 2. 구조화된 임베딩 텍스트 준비
        # 제목을 2회 반복하여 가중치 부여, 카테고리 포함
        def build_embedding_text(chunk: str) -> str:
            """검색 정확도를 위한 구조화된 텍스트 생성"""
            parts = []
            # 제목 가중치: 2회 반복
            parts.append(f"[제목] {title}")
            parts.append(f"[제목] {title}")
            if category:
                parts.append(f"[카테고리] {category}")
            parts.append(f"[본문] {chunk}")
            return "\n".join(parts)
        
        texts_to_embed = [build_embedding_text(chunk) for chunk in chunks]
        
        # DEBUG: 임베딩 텍스트 확인
        if texts_to_embed:
            print(f"DEBUG: Embedding text preview: {texts_to_embed[0][:200]}...")
        
        # 3. 청크들을 임베딩
        embeddings = self._embedding_service.embed_batch(texts_to_embed)
        
        # 4. 벡터 DB에 저장 (원본 청크 저장, 임베딩은 구조화된 텍스트)
        return await self._vector_store.store(
            page_id=page_id,
            space_key=space_key,
            title=title,
            chunks=chunks,
            embeddings=embeddings,
            page_url=page_url
        )
    
    async def delete_document(self, page_id: str) -> int:
        """
        문서의 임베딩 삭제
        
        Args:
            page_id: 페이지 ID
            
        Returns:
            int: 삭제된 청크 수
        """
        return await self._vector_store.delete(page_id)
