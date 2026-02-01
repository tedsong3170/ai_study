"""
Agent Service Configuration
환경 변수 기반 설정 관리
"""
from functools import lru_cache
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """애플리케이션 설정"""
    
    # Database
    database_url: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/confluence_chatbot"
    db_pool_size: int = 5
    
    # Ollama LLM
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "gpt-oss:20b"
    ollama_timeout: int = 60
    
    # Embedding
    embedding_model: str = "all-MiniLM-L6-v2"
    embedding_dimension: int = 384
    
    # Chunking
    chunk_max_size: int = 500
    chunk_overlap: int = 50
    
    # Conversation Memory
    max_chat_history_turns: int = 5
    
    # API
    api_title: str = "Confluence Chatbot Agent"
    api_version: str = "0.1.0"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache
def get_settings() -> Settings:
    """캐시된 설정 인스턴스 반환"""
    return Settings()
