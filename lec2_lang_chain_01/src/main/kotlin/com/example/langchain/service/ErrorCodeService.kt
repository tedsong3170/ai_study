package com.example.langchain.service

import com.example.langchain.repository.ErrorCodeRepository
import dev.langchain4j.model.chat.ChatLanguageModel
import org.springframework.stereotype.Service

/**
 * 에러 코드 AI 질의응답 서비스
 */
@Service
class ErrorCodeService(
    private val errorCodeRepository: ErrorCodeRepository,
    private val chatLanguageModel: ChatLanguageModel
) {

    /**
     * 사용자 질문에 대한 AI 응답 생성
     * 
     * @param query 사용자 질문
     * @return AI 응답
     */
    fun queryErrorCode(query: String): String {
        val errorCodes = errorCodeRepository.findAll()
        
        val prompt = buildPrompt(errorCodes, query)
        
        return chatLanguageModel.generate(prompt)
    }

    private fun buildPrompt(errorCodes: List<com.example.langchain.domain.ErrorCode>, userQuery: String): String {
        val errorCodeContext = if (errorCodes.isEmpty()) {
            "등록된 에러 코드가 없습니다."
        } else {
            errorCodes.joinToString("\n\n") { errorCode ->
                """
                코드: ${errorCode.code}
                메시지: ${errorCode.message}
                설명: ${errorCode.description}
                카테고리: ${errorCode.category}
                """.trimIndent()
            }
        }

        return """
            당신은 에러 코드 전문가입니다. 사용자의 질문에 대해 다음 에러 코드 정보를 바탕으로 친절하고 정확하게 답변해주세요.
            
            [등록된 에러 코드 목록]
            $errorCodeContext
            
            [사용자 질문]
            $userQuery
            
            [답변 가이드]
            - 질문에 해당하는 에러 코드가 있다면 코드, 메시지, 설명을 포함하여 답변하세요.
            - 카테고리에 대한 질문이라면 해당 카테고리의 모든 에러 코드를 나열하세요.
            - 등록되지 않은 에러 코드에 대한 질문이라면 "등록되지 않은 에러 코드입니다"라고 답변하세요.
            - 한글로 답변하세요.
        """.trimIndent()
    }
}
