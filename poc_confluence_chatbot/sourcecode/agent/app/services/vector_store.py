"""
벡터 스토어 서비스 (TDD - Green Phase)
PostgreSQL + pgvector를 사용한 벡터 저장 및 검색
"""
from dataclasses import dataclass
from typing import List, Optional
import asyncpg
from pgvector.asyncpg import register_vector
import numpy as np

from app.config import get_settings


@dataclass
class SearchResult:
    """검색 결과"""
    page_id: str
    title: str
    content: str
    url: str
    score: float


class VectorStore:
    """벡터 저장소 서비스"""
    
    def __init__(self, database_url: str = None):
        """
        Args:
            database_url: PostgreSQL 연결 URL
        """
        settings = get_settings()
        self._database_url = database_url or settings.database_url
        self._pool: asyncpg.Pool = None
    
    async def init_pool(self):
        """연결 풀 초기화"""
        if self._pool is None:
            # asyncpg URL 형식으로 변환 (postgresql+asyncpg:// -> postgresql://)
            url = self._database_url.replace("postgresql+asyncpg://", "postgresql://")
            self._pool = await asyncpg.create_pool(url, min_size=2, max_size=10)
            
            # pgvector 타입 등록
            async with self._pool.acquire() as conn:
                await register_vector(conn)
    
    async def close(self):
        """연결 풀 종료"""
        if self._pool:
            await self._pool.close()
            self._pool = None
    
    async def store(
        self,
        page_id: str,
        space_key: str,
        title: str,
        chunks: List[str],
        embeddings: List[List[float]],
        page_url: str
    ) -> int:
        """
        청크와 임베딩을 저장
        
        Args:
            page_id: Confluence 페이지 ID
            space_key: Confluence Space 키
            title: 문서 제목
            chunks: 텍스트 청크 목록
            embeddings: 임베딩 벡터 목록
            page_url: 페이지 URL
            
        Returns:
            int: 저장된 청크 수
        """
        await self.init_pool()
        
        # 기존 청크 삭제 후 새로 저장
        await self.delete(page_id)
        
        async with self._pool.acquire() as conn:
            # pgvector 타입 재등록 (각 connection마다 필요)
            await register_vector(conn)
            
            for idx, (chunk, embedding) in enumerate(zip(chunks, embeddings)):
                # pgvector는 numpy array 필요
                embedding_np = np.array(embedding, dtype=np.float32)
                await conn.execute(
                    """
                    INSERT INTO document_chunks 
                    (confluence_page_id, confluence_space_key, title, chunk_index, content, embedding, page_url)
                    VALUES ($1, $2, $3, $4, $5, $6, $7)
                    """,
                    page_id, space_key, title, idx, chunk, embedding_np, page_url
                )
        
        return len(chunks)
    
    async def search(
        self,
        query_embedding: List[float],
        top_k: int = 5,
        space_key: Optional[str] = None,
        query_text: Optional[str] = None
    ) -> List[SearchResult]:
        """
        유사도 기반 검색 (제목 키워드 매칭 가중치 포함)
        
        Args:
            query_embedding: 질문 임베딩 벡터
            top_k: 반환할 결과 수
            space_key: 검색 제한할 Space (선택)
            query_text: 원본 질문 텍스트 (가중치 계산용)
            
        Returns:
            List[SearchResult]: 검색 결과 (가중치 적용된 점수로 정렬)
        """
        await self.init_pool()
        
        # query_embedding을 numpy array로 변환
        query_np = np.array(query_embedding, dtype=np.float32)
        print(f"DEBUG: DB Search Vector (first 5): {query_np[:5]}")
        
        # 더 많은 후보를 가져와서 가중치 적용 후 재정렬
        fetch_k = top_k * 3  # 3배수로 가져옴
        
        async with self._pool.acquire() as conn:
            await register_vector(conn)
            
            if space_key:
                rows = await conn.fetch(
                    """
                    SELECT confluence_page_id, title, content, page_url,
                           1 - (embedding <=> $1) as score
                    FROM document_chunks
                    WHERE confluence_space_key = $2
                    ORDER BY embedding <=> $1
                    LIMIT $3
                    """,
                    query_np, space_key, fetch_k
                )
            else:
                rows = await conn.fetch(
                    """
                    SELECT confluence_page_id, title, content, page_url,
                           1 - (embedding <=> $1) as score
                    FROM document_chunks
                    ORDER BY embedding <=> $1
                    LIMIT $2
                    """,
                    query_np, fetch_k
                )
        
        results = []
        for row in rows:
            vector_score = float(row['score'])
            title = row['title']
            
            # 가중치 적용: 제목에 쿼리 키워드가 포함되면 보너스 점수
            title_bonus = 0.0
            if query_text:
                query_words = set(query_text.lower().split())
                title_lower = title.lower()
                # 제목에 쿼리 단어가 포함되면 보너스
                for word in query_words:
                    if len(word) >= 2 and word in title_lower:
                        title_bonus += 0.3  # 단어당 0.3 보너스
                title_bonus = min(title_bonus, 0.5)  # 최대 0.5
            
            # 최종 점수: 벡터 유사도 + 제목 보너스
            final_score = vector_score + title_bonus
            
            results.append(SearchResult(
                page_id=row['confluence_page_id'],
                title=title,
                content=row['content'],
                url=row['page_url'] or '',
                score=final_score
            ))
        
        # 최종 점수로 재정렬 후 top_k만 반환
        results.sort(key=lambda x: x.score, reverse=True)
        return results[:top_k]
    
    async def delete(self, page_id: str) -> int:
        """
        페이지의 모든 청크 삭제
        
        Args:
            page_id: Confluence 페이지 ID
            
        Returns:
            int: 삭제된 청크 수
        """
        await self.init_pool()
        
        async with self._pool.acquire() as conn:
            result = await conn.execute(
                "DELETE FROM document_chunks WHERE confluence_page_id = $1",
                page_id
            )
            # "DELETE 5" 형식에서 숫자 추출
            count = int(result.split()[-1]) if result else 0
            return count
    
    # 테스트용 헬퍼 메서드들
    async def _execute_query(self, *args, **kwargs):
        """쿼리 실행 (테스트 Mock용)"""
        pass
    
    async def _execute_search(self, *args, **kwargs):
        """검색 실행 (테스트 Mock용)"""
        pass
    
    async def _execute_delete(self, *args, **kwargs):
        """삭제 실행 (테스트 Mock용)"""
        pass
