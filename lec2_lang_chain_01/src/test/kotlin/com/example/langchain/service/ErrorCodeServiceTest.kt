package com.example.langchain.service

import com.example.langchain.domain.ErrorCode
import com.example.langchain.repository.ErrorCodeRepository
import dev.langchain4j.model.chat.ChatLanguageModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorCodeServiceTest {

    private lateinit var errorCodeRepository: ErrorCodeRepository
    private lateinit var chatLanguageModel: ChatLanguageModel
    private lateinit var errorCodeService: ErrorCodeService

    @BeforeEach
    fun setUp() {
        errorCodeRepository = mockk()
        chatLanguageModel = mockk()
        errorCodeService = ErrorCodeService(errorCodeRepository, chatLanguageModel)
    }

    @Test
    fun `should query error code and return AI response`() {
        // Given
        val query = "ERR001이 뭐야?"
        val errorCodes = listOf(
            ErrorCode(
                code = "ERR001",
                message = "Invalid credentials",
                description = "The provided username or password is incorrect",
                category = "AUTHENTICATION"
            ),
            ErrorCode(
                code = "ERR002",
                message = "Account locked",
                description = "Your account has been locked",
                category = "AUTHENTICATION"
            )
        )
        val aiResponse = "ERR001은 인증 실패 오류입니다. 사용자 이름 또는 비밀번호가 올바르지 않을 때 발생합니다."

        every { errorCodeRepository.findAll() } returns errorCodes
        every { chatLanguageModel.generate(any<String>()) } returns aiResponse

        // When
        val response = errorCodeService.queryErrorCode(query)

        // Then
        assertNotNull(response)
        assertTrue(response.isNotBlank())
        verify(exactly = 1) { errorCodeRepository.findAll() }
        verify(exactly = 1) { chatLanguageModel.generate(any<String>()) }
    }

    @Test
    fun `should include error code context in prompt`() {
        // Given
        val query = "VALIDATION 카테고리 에러는 뭐가 있어?"
        val errorCodes = listOf(
            ErrorCode(
                code = "ERR101",
                message = "Missing required field",
                description = "A required field was not provided",
                category = "VALIDATION"
            )
        )
        var capturedPrompt = ""

        every { errorCodeRepository.findAll() } returns errorCodes
        every { chatLanguageModel.generate(any<String>()) } answers {
            capturedPrompt = firstArg()
            "VALIDATION 카테고리 에러에는 ERR101이 있습니다."
        }

        // When
        errorCodeService.queryErrorCode(query)

        // Then
        assertContains(capturedPrompt, "ERR101")
        assertContains(capturedPrompt, "Missing required field")
        assertContains(capturedPrompt, "VALIDATION")
    }

    @Test
    fun `should handle empty repository`() {
        // Given
        val query = "에러코드 알려줘"
        val emptyList = emptyList<ErrorCode>()

        every { errorCodeRepository.findAll() } returns emptyList
        every { chatLanguageModel.generate(any<String>()) } returns "등록된 에러코드가 없습니다."

        // When
        val response = errorCodeService.queryErrorCode(query)

        // Then
        assertNotNull(response)
        verify(exactly = 1) { errorCodeRepository.findAll() }
    }

    @Test
    fun `should format error codes in Korean`() {
        // Given
        val query = "에러코드 목록"
        val errorCodes = listOf(
            ErrorCode(
                code = "ERR001",
                message = "Invalid credentials",
                description = "Authentication failed",
                category = "AUTHENTICATION"
            )
        )
        var capturedPrompt = ""

        every { errorCodeRepository.findAll() } returns errorCodes
        every { chatLanguageModel.generate(any<String>()) } answers {
            capturedPrompt = firstArg()
            "응답"
        }

        // When
        errorCodeService.queryErrorCode(query)

        // Then
        // 프롬프트에 한글 설명이 포함되어 있는지 확인
        assertTrue(capturedPrompt.contains("에러") || capturedPrompt.contains("코드"))
    }
}
