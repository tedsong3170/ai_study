package com.example.langchain.service

import dev.langchain4j.model.chat.ChatLanguageModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import com.example.langchain.domain.ErrorCode

/**
 * ErrorCodeService 단위 테스트
 * 패턴 매칭 기반 구조
 */
class ErrorCodeServiceTest {

    private lateinit var errorCodeTools: ErrorCodeTools
    private lateinit var chatLanguageModel: ChatLanguageModel
    private lateinit var errorCodeService: ErrorCodeService

    @BeforeEach
    fun setUp() {
        errorCodeTools = mockk()
        chatLanguageModel = mockk()
        errorCodeService = ErrorCodeService(errorCodeTools, chatLanguageModel)
    }

    @Test
    @DisplayName("ERR 코드 패턴 매칭 - 성공")
    fun queryErrorCode_withErrorCode() {
        // given
        val query = "ERR001"
        val errorCode = ErrorCode("ERR001", "Invalid credentials", "Test description", "AUTHENTICATION")
        
        every { errorCodeTools.getErrorCodeByCode("ERR001") } returns errorCode
        every { chatLanguageModel.generate(any<String>()) } returns "테스트 응답"

        // when
        val response = errorCodeService.queryErrorCode(query)

        // then
        assertThat(response).isEqualTo("테스트 응답")
        verify(exactly = 1) { errorCodeTools.getErrorCodeByCode("ERR001") }
        verify(exactly = 1) { chatLanguageModel.generate(any<String>()) }
    }

    @Test
    @DisplayName("카테고리 키워드 매칭 - 인증")
    fun queryErrorCode_withAuthKeyword() {
        // given
        val query = "인증 관련 에러"
        val errorCodes = listOf(
            ErrorCode("ERR001", "Invalid credentials", "...", "AUTHENTICATION"),
            ErrorCode("ERR002", "Account locked", "...", "AUTHENTICATION")
        )
        
        every { errorCodeTools.getErrorCodesByCategory("AUTHENTICATION") } returns errorCodes
        every { chatLanguageModel.generate(any<String>()) } returns "인증 에러 응답"

        // when
        val response = errorCodeService.queryErrorCode(query)

        // then
        assertThat(response).isEqualTo("인증 에러 응답")
        verify(exactly = 1) { errorCodeTools.getErrorCodesByCategory("AUTHENTICATION") }
    }

    @Test
    @DisplayName("매칭 실패 - 기본 메시지 반환")
    fun queryErrorCode_noMatch() {
        // given
        val query = "알 수 없는 질문"

        // when
        val response = errorCodeService.queryErrorCode(query)

        // then
        assertThat(response).contains("요청하신 에러 코드 정보를 찾을 수 없습니다")
    }
}
