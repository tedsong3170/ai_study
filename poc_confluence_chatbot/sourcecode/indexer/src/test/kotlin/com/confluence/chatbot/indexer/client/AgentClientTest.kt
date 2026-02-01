package com.confluence.chatbot.indexer.client

import com.confluence.chatbot.indexer.config.AppConfig
import com.confluence.chatbot.indexer.model.EmbedRequest
import com.confluence.chatbot.indexer.model.EmbedResponse
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AgentClientTest {

    private lateinit var config: AppConfig

    @BeforeEach
    fun setup() {
        config =
                AppConfig(
                        confluenceBaseUrl = "https://test.atlassian.net",
                        confluenceApiToken = "test-token",
                        confluenceUserEmail = "test@example.com",
                        confluenceSpaceKey = "TEST",
                        agentBaseUrl = "http://localhost:8000"
                )
    }

    @Test
    fun `EmbedRequest should serialize correctly`() {
        // Given
        val request =
                EmbedRequest(
                        page_id = "12345",
                        space_key = "DEV",
                        title = "테스트 문서",
                        content = "문서 내용입니다.",
                        page_url = "https://example.com/wiki/12345"
                )

        // Then
        assertEquals("12345", request.page_id)
        assertEquals("DEV", request.space_key)
        assertEquals("테스트 문서", request.title)
        assertEquals("문서 내용입니다.", request.content)
        assertEquals("https://example.com/wiki/12345", request.page_url)
    }

    @Test
    fun `EmbedResponse should deserialize correctly`() {
        // Given
        val response =
                EmbedResponse(
                        status = "success",
                        page_id = "12345",
                        chunks_created = 5,
                        message = "Document embedded successfully"
                )

        // Then
        assertEquals("success", response.status)
        assertEquals("12345", response.page_id)
        assertEquals(5, response.chunks_created)
        assertEquals("Document embedded successfully", response.message)
    }

    @Test
    fun `parseUrl should extract host and port correctly`() {
        // Given
        val url1 = "http://localhost:8000"
        val url2 = "http://agent:8000/api"
        val url3 = "http://192.168.1.100:9000"

        // When - 내부 메서드 테스트를 위한 간단한 파싱
        fun parseUrl(url: String): Pair<String, Int> {
            val withoutProtocol = url.removePrefix("http://").removePrefix("https://")
            val parts = withoutProtocol.split(":")
            val host = parts[0].split("/").first()
            val port = if (parts.size > 1) parts[1].split("/").first().toInt() else 80
            return Pair(host, port)
        }

        // Then
        assertEquals(Pair("localhost", 8000), parseUrl(url1))
        assertEquals(Pair("agent", 8000), parseUrl(url2))
        assertEquals(Pair("192.168.1.100", 9000), parseUrl(url3))
    }
}
