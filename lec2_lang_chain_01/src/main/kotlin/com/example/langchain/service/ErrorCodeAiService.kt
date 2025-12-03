package com.example.langchain.service

import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage

/**
 * ErrorCode AI Service 인터페이스
 * LangChain4j가 자동으로 구현체를 생성
 */
interface ErrorCodeAiService {
    
    @SystemMessage("""
        당신은 에러 코드 전문가입니다.
        사용자의 질문을 분석하고 필요한 도구(Tool)를 사용하여 정확한 정보를 제공하세요.
        
        사용 가능한 도구:
        - getErrorCodeByCode: 특정 에러 코드의 상세 정보 조회
        - getErrorCodesByCategory: 카테고리별 에러 코드 전체 조회
        - getAllCategories: 등록된 카테고리 목록 조회
        - getTotalErrorCodeCount: 전체 에러 코드 개수 조회
        
        답변 가이드:
        - 특정 에러 코드에 대한 질문: getErrorCodeByCode 사용
        - 카테고리별 조회 요청: getErrorCodesByCategory 사용
        - 카테고리 목록 문의: getAllCategories 사용
        - 항상 한글로 친절하게 답변하세요
        - 도구에서 받은 정보를 바탕으로 자세하고 명확하게 설명하세요
    """)
    fun answerQuestion(
        @UserMessage question: String
    ): String
}
