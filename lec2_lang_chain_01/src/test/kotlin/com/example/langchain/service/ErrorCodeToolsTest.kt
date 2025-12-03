package com.example.langchain.service

import com.example.langchain.domain.ErrorCode
import com.example.langchain.repository.ErrorCodeRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * ErrorCodeTools 단위 테스트
 */
class ErrorCodeToolsTest {

    private lateinit var errorCodeRepository: ErrorCodeRepository
    private lateinit var errorCodeTools: ErrorCodeTools

    @BeforeEach
    fun setUp() {
        errorCodeRepository = mockk()
        errorCodeTools = ErrorCodeTools(errorCodeRepository)
    }

    @Test
    @DisplayName("특정 에러 코드 조회 - 성공")
    fun getErrorCodeByCode_exists() {
        // given
        val code = "ERR001"
        val errorCode = ErrorCode(
            code = "ERR001",
            message = "Invalid credentials",
            description = "Authentication failed",
            category = "AUTHENTICATION"
        )

        every { errorCodeRepository.findByCode(code) } returns errorCode

        // when
        val result = errorCodeTools.getErrorCodeByCode(code)

        // then
        assertThat(result).isNotNull
        assertThat(result?.code).isEqualTo("ERR001")
        assertThat(result?.message).isEqualTo("Invalid credentials")
    }

    @Test
    @DisplayName("특정 에러 코드 조회 - 없음")
    fun getErrorCodeByCode_notExists() {
        // given
        val code = "ERR999"

        every { errorCodeRepository.findByCode(code) } returns null

        // when
        val result = errorCodeTools.getErrorCodeByCode(code)

        // then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("카테고리별 에러 코드 조회")
    fun getErrorCodesByCategory() {
        // given
        val category = "AUTHENTICATION"
        val errorCodes = listOf(
            ErrorCode("ERR001", "Invalid credentials", "...", "AUTHENTICATION"),
            ErrorCode("ERR002", "Account locked", "...", "AUTHENTICATION")
        )

        every { errorCodeRepository.findByCategory(category) } returns errorCodes

        // when
        val result = errorCodeTools.getErrorCodesByCategory(category)

        // then
        assertThat(result).hasSize(2)
        assertThat(result).allMatch { it.category == "AUTHENTICATION" }
    }
}

