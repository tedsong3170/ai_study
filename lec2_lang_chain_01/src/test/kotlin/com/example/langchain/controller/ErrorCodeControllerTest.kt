package com.example.langchain.controller

import com.example.langchain.domain.ErrorCode
import com.example.langchain.repository.ErrorCodeRepository
import com.example.langchain.service.ErrorCodeService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ErrorCodeController::class)
class ErrorCodeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var errorCodeService: ErrorCodeService

    @Autowired
    private lateinit var errorCodeRepository: ErrorCodeRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @TestConfiguration
    class TestConfig {
        @Bean
        fun errorCodeService(): ErrorCodeService = mockk(relaxed = true)

        @Bean
        fun errorCodeRepository(): ErrorCodeRepository = mockk(relaxed = true)
    }

    @BeforeEach
    fun setUp() {
        val sampleErrorCodes = listOf(
            ErrorCode("ERR001", "Invalid credentials", "Authentication failed", "AUTHENTICATION"),
            ErrorCode("ERR002", "Account locked", "Your account has been locked", "AUTHENTICATION"),
            ErrorCode("ERR101", "Missing required field", "Required field not provided", "VALIDATION")
        )

        every { errorCodeRepository.findAll() } returns sampleErrorCodes
        every { errorCodeRepository.findByCode("ERR001") } returns sampleErrorCodes[0]
        every { errorCodeRepository.findByCode("NONEXISTENT") } returns null
    }

    @Test
    fun `should return all error codes`() {
        // When & Then
        mockMvc.perform(get("/api/error-codes"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `should return error code by code`() {
        // When & Then
        mockMvc.perform(get("/api/error-codes/ERR001"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("ERR001"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.category").exists())
    }

    @Test
    fun `should return 404 when error code not found`() {
        // When & Then
        mockMvc.perform(get("/api/error-codes/NONEXISTENT"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should query error code and return AI response`() {
        // Given
        val queryRequest = mapOf("query" to "ERR001이 뭐야?")
        val aiResponse = "ERR001은 인증 실패 오류입니다."

        every { errorCodeService.queryErrorCode(any()) } returns aiResponse

        // When & Then
        mockMvc.perform(
            post("/api/error-codes/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.answer").value(aiResponse))
    }

    @Test
    fun `should return 400 when query is missing`() {
        // Given
        val emptyRequest = mapOf<String, String>()

        // When & Then
        mockMvc.perform(
            post("/api/error-codes/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when query is blank`() {
        // Given
        val blankQueryRequest = mapOf("query" to "")

        // When & Then
        mockMvc.perform(
            post("/api/error-codes/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankQueryRequest))
        )
            .andExpect(status().isBadRequest)
    }
}
