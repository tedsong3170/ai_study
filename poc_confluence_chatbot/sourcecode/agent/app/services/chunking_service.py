"""
청킹 서비스 (TDD - Green Phase)
텍스트를 적절한 크기의 청크로 분할
"""
from typing import List

from app.config import get_settings


class ChunkingService:
    """텍스트 청킹 서비스"""
    
    def __init__(self, max_chunk_size: int = None, overlap: int = None):
        """
        Args:
            max_chunk_size: 청크 최대 크기 (기본: 500)
            overlap: 청크 간 오버랩 크기 (기본: 50)
        """
        settings = get_settings()
        self._max_chunk_size = max_chunk_size or settings.chunk_max_size
        self._overlap = overlap or settings.chunk_overlap
    
    def split(self, text: str) -> List[str]:
        """
        텍스트를 청크로 분할
        
        Args:
            text: 분할할 텍스트
            
        Returns:
            List[str]: 분할된 청크 목록
        """
        # 빈 텍스트 처리
        text = text.strip()
        if not text:
            return []
        
        # 텍스트가 max_chunk_size보다 작으면 그대로 반환
        if len(text) <= self._max_chunk_size:
            return [text]
        
        chunks = []
        start = 0
        
        while start < len(text):
            # 청크 끝 위치 계산
            end = start + self._max_chunk_size
            
            # 텍스트 끝을 넘어가면 조정
            if end >= len(text):
                chunks.append(text[start:])
                break
            
            # 문장 끝(마침표, 물음표, 느낌표) 또는 공백에서 자르기
            break_point = self._find_break_point(text, start, end)
            
            chunks.append(text[start:break_point])
            
            # 다음 시작점 (오버랩 적용)
            start = break_point - self._overlap
            if start < 0:
                start = 0
        
        return chunks
    
    def _find_break_point(self, text: str, start: int, end: int) -> int:
        """
        적절한 분할 지점 찾기
        
        Args:
            text: 전체 텍스트
            start: 청크 시작 위치
            end: 청크 끝 위치
            
        Returns:
            int: 분할 위치
        """
        # 마침표, 물음표, 느낌표 위치 찾기 (뒤에서부터)
        for i in range(end, start + self._max_chunk_size // 2, -1):
            if i < len(text) and text[i] in '.?!。':
                return i + 1
        
        # 문장 끝이 없으면 공백에서 자르기
        for i in range(end, start + self._max_chunk_size // 2, -1):
            if i < len(text) and text[i] in ' \n\t':
                return i + 1
        
        # 적절한 위치가 없으면 그냥 end에서 자르기
        return end
