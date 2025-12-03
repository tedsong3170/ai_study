package com.example.langchain.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ErrorCodeTest {

    @Test
    fun `should create ErrorCode with valid parameters`() {
        // Given
        val code = "ERR001"
        val message = "Invalid credentials"
        val description = "The provided username or password is incorrect"
        val category = "AUTHENTICATION"

        // When
        val errorCode = ErrorCode(
            code = code,
            message = message,
            description = description,
            category = category
        )

        // Then
        assertEquals(code, errorCode.code)
        assertEquals(message, errorCode.message)
        assertEquals(description, errorCode.description)
        assertEquals(category, errorCode.category)
    }

    @Test
    fun `should fail when code is blank`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> {
            ErrorCode(
                code = "",
                message = "Invalid credentials",
                description = "The provided username or password is incorrect",
                category = "AUTHENTICATION"
            )
        }
    }

    @Test
    fun `should fail when message is blank`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> {
            ErrorCode(
                code = "ERR001",
                message = "",
                description = "The provided username or password is incorrect",
                category = "AUTHENTICATION"
            )
        }
    }

    @Test
    fun `should fail when description is blank`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> {
            ErrorCode(
                code = "ERR001",
                message = "Invalid credentials",
                description = "",
                category = "AUTHENTICATION"
            )
        }
    }

    @Test
    fun `should fail when category is blank`() {
        // Given & When & Then
        assertThrows<IllegalArgumentException> {
            ErrorCode(
                code = "ERR001",
                message = "Invalid credentials",
                description = "The provided username or password is incorrect",
                category = ""
            )
        }
    }
}
