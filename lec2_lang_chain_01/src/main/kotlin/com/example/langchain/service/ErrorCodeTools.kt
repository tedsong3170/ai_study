package com.example.langchain.service

import com.example.langchain.domain.ErrorCode
import com.example.langchain.repository.ErrorCodeRepository
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component

/**
 * LangChain4j Tools for ErrorCode
 * AI가 호출할 수 있는 도구 메서드 정의
 */
@Component
class ErrorCodeTools(
    private val errorCodeRepository: ErrorCodeRepository
) {
    
    /**
     * 특정 에러 코드의 상세 정보를 조회합니다
     */
    @Tool("특정 에러 코드의 상세 정보를 조회합니다. 사용자가 특정 에러 코드(예: ERR001)에 대해 물어볼 때 사용하세요.")
    fun getErrorCodeByCode(
        @P("조회할 에러 코드 (예: ERR001)") code: String
    ): ErrorCode? {
        return errorCodeRepository.findByCode(code)
    }
    
    /**
     * 특정 카테고리의 모든 에러 코드를 조회합니다
     */
    @Tool("특정 카테고리의 모든 에러 코드를 조회합니다. 사용자가 특정 카테고리(예: AUTHENTICATION, VALIDATION)의 모든 에러를 알고 싶을 때 사용하세요.")
    fun getErrorCodesByCategory(
        @P("카테고리 이름 (예: AUTHENTICATION, VALIDATION, RESOURCE, AUTHORIZATION)") 
        category: String
    ): List<ErrorCode> {
        return errorCodeRepository.findByCategory(category)
    }
}
