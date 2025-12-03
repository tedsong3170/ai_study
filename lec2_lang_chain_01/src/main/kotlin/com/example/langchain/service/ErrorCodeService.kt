package com.example.langchain.service

import dev.langchain4j.model.chat.ChatLanguageModel
import org.springframework.stereotype.Service

/**
 * 에러 코드 AI 질의응답 서비스
 * 간소화된 Tool 매칭 방식
 */
@Service
class ErrorCodeService(
    private val errorCodeTools: ErrorCodeTools,
    private val chatLanguageModel: ChatLanguageModel
) {

    /**
     * 사용자 질문에 대한 AI 응답 생성
     * 패턴 매칭으로 Tool을 선택하고 최소한의 프롬프트로 답변 생성
     * 
     * @param query 사용자 질문
     * @return AI 응답
     */
    fun queryErrorCode(query: String): String {
        // 1. 간단한 패턴 매칭으로 Tool 선택
        val toolResult = matchAndExecuteTool(query)
        
        // 2. Tool 결과를 기반으로 간단한 프롬프트로 답변 생성
        return if (toolResult != null) {
            val prompt = "다음 정보를 한글로 설명해주세요:\n$toolResult"
            chatLanguageModel.generate(prompt)
        } else {
            "죄송합니다. 요청하신 에러 코드 정보를 찾을 수 없습니다. ERR001, ERR002 등의 코드나 AUTHENTICATION, VALIDATION 등의 카테고리로 질문해주세요."
        }
    }
    
    /**
     * 패턴 매칭으로 Tool 실행
     */
    private fun matchAndExecuteTool(query: String): String? {
        val upperQuery = query.uppercase()
        
        // ERR 패턴 매칭 (ERR001, ERR002 등)
        val errorCodePattern = Regex("ERR\\d{3}")
        errorCodePattern.find(upperQuery)?.let { match ->
            val code = match.value
            return errorCodeTools.getErrorCodeByCode(code)?.let {
                """코드: ${it.code}
메시지: ${it.message}
설명: ${it.description}
카테고리: ${it.category}"""
            }
        }
        
        // 카테고리 매칭
        val categories = listOf("AUTHENTICATION", "VALIDATION", "RESOURCE", "AUTHORIZATION")
        categories.forEach { category ->
            if (upperQuery.contains(category) || 
                (category == "AUTHENTICATION" && matchesAuthKeywords(upperQuery)) ||
                (category == "VALIDATION" && matchesValidationKeywords(upperQuery)) ||
                (category == "RESOURCE" && matchesResourceKeywords(upperQuery)) ||
                (category == "AUTHORIZATION" && matchesAuthorizationKeywords(upperQuery))) {
                
                val errorCodes = errorCodeTools.getErrorCodesByCategory(category)
                if (errorCodes.isNotEmpty()) {
                    return errorCodes.joinToString("\n\n") {
                        "코드: ${it.code}, 메시지: ${it.message}"
                    }
                }
            }
        }
        
        return null
    }
    
    private fun matchesAuthKeywords(query: String): Boolean {
        val keywords = listOf("인증", "로그인", "LOGIN", "비밀번호", "PASSWORD", "CREDENTIAL")
        return keywords.any { query.contains(it) }
    }
    
    private fun matchesValidationKeywords(query: String): Boolean {
        val keywords = listOf("검증", "유효성", "VALID", "입력", "형식", "FORMAT")
        return keywords.any { query.contains(it) }
    }
    
    private fun matchesResourceKeywords(query: String): Boolean {
        val keywords = listOf("리소스", "자원", "RESOURCE", "찾을수없", "NOT FOUND")
        return keywords.any { query.contains(it) }
    }
    
    private fun matchesAuthorizationKeywords(query: String): Boolean {
        val keywords = listOf("권한", "허가", "PERMISSION", "ROLE", "역할")
        return keywords.any { query.contains(it) }
    }
}


