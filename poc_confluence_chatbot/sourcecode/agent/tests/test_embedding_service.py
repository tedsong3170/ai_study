"""
임베딩 서비스 테스트 (TDD - Red Phase)
"""
import pytest
from app.services.embedding_service import EmbeddingService


class TestEmbeddingService:
    """임베딩 서비스 테스트"""
    
    @pytest.fixture
    def embedding_service(self):
        """테스트용 임베딩 서비스 인스턴스"""
        return EmbeddingService()
    
    def test_embed_text_returns_vector(self, embedding_service):
        """텍스트를 임베딩하면 벡터를 반환해야 한다"""
        # Given
        text = "안녕하세요. 이것은 테스트 문장입니다."
        
        # When
        result = embedding_service.embed(text)
        
        # Then
        assert result is not None
        assert isinstance(result, list)
        assert len(result) == 384  # all-MiniLM-L6-v2 dimension
        assert all(isinstance(x, float) for x in result)
    
    def test_embed_empty_text_returns_vector(self, embedding_service):
        """빈 텍스트도 벡터를 반환해야 한다"""
        # Given
        text = ""
        
        # When
        result = embedding_service.embed(text)
        
        # Then
        assert result is not None
        assert len(result) == 384
    
    def test_embed_batch_returns_multiple_vectors(self, embedding_service):
        """여러 텍스트를 배치로 임베딩하면 여러 벡터를 반환해야 한다"""
        # Given
        texts = [
            "첫 번째 문장입니다.",
            "두 번째 문장입니다.",
            "세 번째 문장입니다."
        ]
        
        # When
        result = embedding_service.embed_batch(texts)
        
        # Then
        assert len(result) == 3
        assert all(len(vec) == 384 for vec in result)
    
    def test_embed_same_text_returns_same_vector(self, embedding_service):
        """같은 텍스트는 같은 벡터를 반환해야 한다"""
        # Given
        text = "동일한 문장입니다."
        
        # When
        result1 = embedding_service.embed(text)
        result2 = embedding_service.embed(text)
        
        # Then
        assert result1 == result2
