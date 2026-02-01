"""
임베딩 서비스 (TDD - Green Phase)
sentence-transformers를 사용한 텍스트 임베딩
"""
from typing import List
from sentence_transformers import SentenceTransformer

from app.config import get_settings


class EmbeddingService:
    """텍스트 임베딩 서비스"""
    
    def __init__(self, model_name: str = None):
        """
        Args:
            model_name: 사용할 임베딩 모델 (기본: all-MiniLM-L6-v2)
        """
        settings = get_settings()
        self._model_name = model_name or settings.embedding_model
        print(f"DEBUG: Loading embedding model: {self._model_name}")
        self._model: SentenceTransformer = None
        self._dimension = settings.embedding_dimension
    
    @property
    def model(self) -> SentenceTransformer:
        """Lazy loading으로 모델 초기화"""
        if self._model is None:
            self._model = SentenceTransformer(self._model_name)
        return self._model
    
    @property
    def dimension(self) -> int:
        """임베딩 벡터 차원"""
        return self._dimension
    
    def embed(self, text: str) -> List[float]:
        """
        텍스트를 벡터로 변환
        
        Args:
            text: 임베딩할 텍스트
            
        Returns:
            List[float]: 임베딩 벡터 (384차원)
        """
        embedding = self.model.encode(text, convert_to_numpy=True)
        return embedding.tolist()
    
    def embed_batch(self, texts: List[str]) -> List[List[float]]:
        """
        여러 텍스트를 배치로 임베딩
        
        Args:
            texts: 임베딩할 텍스트 목록
            
        Returns:
            List[List[float]]: 임베딩 벡터 목록
        """
        embeddings = self.model.encode(texts, convert_to_numpy=True)
        return [embedding.tolist() for embedding in embeddings]
