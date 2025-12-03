package com.example.langchain.repository

import com.example.langchain.domain.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryErrorCodeRepositoryTest {

    private lateinit var repository: ErrorCodeRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryErrorCodeRepository()
    }

    @Test
    fun `should return all error codes`() {
        // When
        val errorCodes = repository.findAll()

        // Then
        assertTrue(errorCodes.isNotEmpty())
        assertTrue(errorCodes.size >= 5) // 최소 5개의 샘플 데이터가 있어야 함
    }

    @Test
    fun `should find error code by code`() {
        // Given
        val allCodes = repository.findAll()
        val firstCode = allCodes.first()

        // When
        val found = repository.findByCode(firstCode.code)

        // Then
        assertNotNull(found)
        assertEquals(firstCode.code, found.code)
        assertEquals(firstCode.message, found.message)
        assertEquals(firstCode.description, found.description)
        assertEquals(firstCode.category, found.category)
    }

    @Test
    fun `should return null when code does not exist`() {
        // When
        val found = repository.findByCode("NONEXISTENT")

        // Then
        assertNull(found)
    }

    @Test
    fun `should find error codes by category`() {
        // Given
        val allCodes = repository.findAll()
        val firstCategory = allCodes.first().category

        // When
        val foundCodes = repository.findByCategory(firstCategory)

        // Then
        assertTrue(foundCodes.isNotEmpty())
        foundCodes.forEach { errorCode ->
            assertEquals(firstCategory, errorCode.category)
        }
    }

    @Test
    fun `should return empty list when category does not exist`() {
        // When
        val foundCodes = repository.findByCategory("NONEXISTENT_CATEGORY")

        // Then
        assertTrue(foundCodes.isEmpty())
    }

    @Test
    fun `should contain authentication error codes`() {
        // When
        val authErrors = repository.findByCategory("AUTHENTICATION")

        // Then
        assertTrue(authErrors.isNotEmpty())
    }

    @Test
    fun `should contain validation error codes`() {
        // When
        val validationErrors = repository.findByCategory("VALIDATION")

        // Then
        assertTrue(validationErrors.isNotEmpty())
    }
}
