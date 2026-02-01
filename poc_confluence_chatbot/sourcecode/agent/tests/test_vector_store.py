"""
벡터 스토어 서비스 테스트 (TDD - Red Phase)
실제 DB 연결 없이 Mock으로 테스트
"""
import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from app.services.vector_store import VectorStore, SearchResult


class TestVectorStore:
    """벡터 스토어 테스트"""
    
    @pytest.fixture
    def vector_store(self):
        """테스트용 벡터 스토어 인스턴스"""
        store = VectorStore(database_url="postgresql+asyncpg://test:test@localhost:5432/test")
        # DB 연결을 Mock으로 대체
        store._pool = MagicMock()
        return store
    
    @pytest.mark.asyncio
    async def test_store_saves_chunks_and_embeddings(self, vector_store):
        """청크와 임베딩을 저장해야 한다"""
        # Given
        page_id = "12345"
        space_key = "DEV"
        title = "테스트 문서"
        chunks = ["첫 번째 청크", "두 번째 청크"]
        embeddings = [[0.1] * 384, [0.2] * 384]
        page_url = "https://example.com/wiki/12345"
        
        # Mock DB connection
        mock_conn = AsyncMock()
        mock_conn.execute = AsyncMock()
        vector_store._pool.acquire = MagicMock(return_value=AsyncMock(__aenter__=AsyncMock(return_value=mock_conn), __aexit__=AsyncMock()))
        
        # Mock init_pool and delete
        with patch.object(vector_store, 'init_pool', new_callable=AsyncMock):
            with patch.object(vector_store, 'delete', new_callable=AsyncMock, return_value=0):
                result = await vector_store.store(
                    page_id=page_id,
                    space_key=space_key,
                    title=title,
                    chunks=chunks,
                    embeddings=embeddings,
                    page_url=page_url
                )
        
        # Then
        assert result == len(chunks)
    
    @pytest.mark.asyncio
    async def test_search_returns_similar_documents(self, vector_store):
        """유사한 문서를 검색해야 한다"""
        # Given
        query_embedding = [0.1] * 384
        top_k = 3
        
        # Mock DB fetch result
        mock_rows = [
            {"confluence_page_id": "1", "title": "문서1", "content": "내용1", "page_url": "url1", "score": 0.95},
            {"confluence_page_id": "2", "title": "문서2", "content": "내용2", "page_url": "url2", "score": 0.85},
        ]
        mock_conn = AsyncMock()
        mock_conn.fetch = AsyncMock(return_value=mock_rows)
        vector_store._pool.acquire = MagicMock(return_value=AsyncMock(__aenter__=AsyncMock(return_value=mock_conn), __aexit__=AsyncMock()))
        
        # When
        with patch.object(vector_store, 'init_pool', new_callable=AsyncMock):
            results = await vector_store.search(query_embedding, top_k)
        
        # Then
        assert len(results) == 2
        assert results[0].score == 0.95
        assert results[0].page_id == "1"
    
    @pytest.mark.asyncio
    async def test_delete_removes_all_chunks_for_page(self, vector_store):
        """페이지의 모든 청크를 삭제해야 한다"""
        # Given
        page_id = "12345"
        
        # Mock DB execute result
        mock_conn = AsyncMock()
        mock_conn.execute = AsyncMock(return_value="DELETE 5")
        vector_store._pool.acquire = MagicMock(return_value=AsyncMock(__aenter__=AsyncMock(return_value=mock_conn), __aexit__=AsyncMock()))
        
        # When
        with patch.object(vector_store, 'init_pool', new_callable=AsyncMock):
            result = await vector_store.delete(page_id)
        
        # Then
        assert result == 5
