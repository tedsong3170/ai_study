"""
Health Check API
"""
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()


class HealthResponse(BaseModel):
    """헬스체크 응답"""
    status: str
    version: str
    dependencies: dict


@router.get("/health", response_model=HealthResponse)
async def health_check() -> HealthResponse:
    """
    서비스 상태 확인
    
    Returns:
        HealthResponse: 서비스 및 의존성 상태
    """
    # TODO: 실제 의존성 상태 체크 구현
    return HealthResponse(
        status="healthy",
        version="0.1.0",
        dependencies={
            "postgres": "healthy",
            "ollama": "healthy"
        }
    )
