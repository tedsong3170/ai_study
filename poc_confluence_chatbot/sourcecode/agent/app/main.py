"""
Agent Service - FastAPI Application
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.api import embed, query, health

settings = get_settings()

app = FastAPI(
    title=settings.api_title,
    version=settings.api_version,
    description="Confluence Chatbot RAG Agent Service"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(embed.router, tags=["Embedding"])
app.include_router(query.router, tags=["Query"])
app.include_router(health.router, tags=["Health"])


@app.on_event("startup")
async def startup_event():
    """애플리케이션 시작 시 초기화"""
    # TODO: DB 연결 풀 초기화
    # TODO: 임베딩 모델 로딩
    pass


@app.on_event("shutdown")
async def shutdown_event():
    """애플리케이션 종료 시 정리"""
    # TODO: DB 연결 풀 정리
    pass
