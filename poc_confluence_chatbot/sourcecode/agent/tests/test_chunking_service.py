"""
청킹 서비스 테스트 (TDD - Red Phase)
"""
import pytest
from app.services.chunking_service import ChunkingService


class TestChunkingService:
    """청킹 서비스 테스트"""
    
    @pytest.fixture
    def chunking_service(self):
        """테스트용 청킹 서비스 인스턴스"""
        return ChunkingService(max_chunk_size=100, overlap=20)
    
    def test_split_short_text_returns_single_chunk(self, chunking_service):
        """짧은 텍스트는 단일 청크로 반환해야 한다"""
        # Given
        text = "짧은 문장입니다."
        
        # When
        chunks = chunking_service.split(text)
        
        # Then
        assert len(chunks) == 1
        assert chunks[0] == text
    
    def test_split_long_text_returns_multiple_chunks(self, chunking_service):
        """긴 텍스트는 여러 청크로 분할해야 한다"""
        # Given
        text = "이것은 매우 긴 문장입니다. " * 20  # 약 300자
        
        # When
        chunks = chunking_service.split(text)
        
        # Then
        assert len(chunks) > 1
        for chunk in chunks:
            assert len(chunk) <= 100  # max_chunk_size
    
    def test_chunks_have_overlap(self, chunking_service):
        """청크 간 오버랩이 있어야 한다"""
        # Given
        text = "A" * 50 + "B" * 50 + "C" * 50  # 150자
        
        # When
        chunks = chunking_service.split(text)
        
        # Then
        # 첫 청크의 끝부분이 다음 청크의 시작부분과 겹쳐야 함
        if len(chunks) > 1:
            overlap_text = chunks[0][-20:]  # 마지막 20자
            assert overlap_text in chunks[1]  # 다음 청크에 포함
    
    def test_empty_text_returns_empty_list(self, chunking_service):
        """빈 텍스트는 빈 리스트를 반환해야 한다"""
        # Given
        text = ""
        
        # When
        chunks = chunking_service.split(text)
        
        # Then
        assert chunks == []
    
    def test_whitespace_only_returns_empty_list(self, chunking_service):
        """공백만 있는 텍스트는 빈 리스트를 반환해야 한다"""
        # Given
        text = "   \n\t  "
        
        # When
        chunks = chunking_service.split(text)
        
        # Then
        assert chunks == []
