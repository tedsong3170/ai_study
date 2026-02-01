package com.confluence.chatbot.indexer.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `ConfluencePage should hold all fields correctly`() {
        // Given
        val page =
                ConfluencePage(
                        id = "12345",
                        spaceKey = "DEV",
                        title = "테스트 페이지",
                        content = "페이지 내용",
                        url = "https://example.atlassian.net/wiki/page/12345",
                        lastModified = "2026-01-20T00:00:00Z"
                )

        // Then
        assertEquals("12345", page.id)
        assertEquals("DEV", page.spaceKey)
        assertEquals("테스트 페이지", page.title)
        assertEquals("페이지 내용", page.content)
        assertEquals("https://example.atlassian.net/wiki/page/12345", page.url)
        assertEquals("2026-01-20T00:00:00Z", page.lastModified)
    }

    @Test
    fun `ConfluencePage should allow null lastModified`() {
        // Given
        val page =
                ConfluencePage(
                        id = "12345",
                        spaceKey = "DEV",
                        title = "테스트",
                        content = "내용",
                        url = "url"
                )

        // Then
        assertNull(page.lastModified)
    }

    @Test
    fun `IndexingResult should track document counts`() {
        // Given
        val result =
                IndexingResult(
                        jobId = "job-123",
                        totalDocuments = 10,
                        processedDocuments = 8,
                        failedDocuments = 2,
                        startedAt = "2026-01-20T00:00:00Z",
                        completedAt = "2026-01-20T00:01:00Z"
                )

        // Then
        assertEquals("job-123", result.jobId)
        assertEquals(10, result.totalDocuments)
        assertEquals(8, result.processedDocuments)
        assertEquals(2, result.failedDocuments)
        assertNotNull(result.completedAt)
    }

    @Test
    fun `EmbedRequest should serialize to JSON`() {
        // Given
        val request =
                EmbedRequest(
                        page_id = "123",
                        space_key = "DEV",
                        title = "Test",
                        content = "Content",
                        page_url = "http://example.com"
                )

        // When
        val jsonString = json.encodeToString(request)

        // Then
        assertTrue(jsonString.contains("\"page_id\":\"123\""))
        assertTrue(jsonString.contains("\"space_key\":\"DEV\""))
    }

    @Test
    fun `EmbedResponse should deserialize from JSON`() {
        // Given
        val jsonString =
                """
            {"status":"success","page_id":"123","chunks_created":5,"message":"ok"}
        """.trimIndent()

        // When
        val response = json.decodeFromString<EmbedResponse>(jsonString)

        // Then
        assertEquals("success", response.status)
        assertEquals("123", response.page_id)
        assertEquals(5, response.chunks_created)
        assertEquals("ok", response.message)
    }
}
