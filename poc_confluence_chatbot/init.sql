-- PostgreSQL 초기화 스크립트
-- pgvector 확장 활성화 및 테이블 생성

-- pgvector 확장 설치
CREATE EXTENSION IF NOT EXISTS vector;

-- document_chunks 테이블 생성
CREATE TABLE IF NOT EXISTS document_chunks (
    id SERIAL PRIMARY KEY,
    confluence_page_id VARCHAR(255) NOT NULL,
    confluence_space_key VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding vector(384),  -- 384 dimensions for all-MiniLM-L6-v2
    page_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_document_chunks_page_id 
    ON document_chunks(confluence_page_id);

CREATE INDEX IF NOT EXISTS idx_document_chunks_space_key 
    ON document_chunks(confluence_space_key);

-- 벡터 검색용 IVFFlat 인덱스 (데이터가 충분히 쌓인 후 생성 권장)
-- CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding 
--     ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 권한 부여
GRANT ALL PRIVILEGES ON TABLE document_chunks TO chatbot;
GRANT USAGE, SELECT ON SEQUENCE document_chunks_id_seq TO chatbot;

-- 초기 데이터 확인 메시지
DO $$
BEGIN
    RAISE NOTICE 'Database initialized with pgvector extension';
END $$;
