package com.confluence.chatbot.indexer.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AppConfigTest {

    @Test
    fun `should create config with all required fields`() {
        // Given
        val config =
                AppConfig(
                        confluenceBaseUrl = "https://test.atlassian.net",
                        confluenceApiToken = "test-token",
                        confluenceUserEmail = "test@example.com",
                        confluenceSpaceKey = "TEST",
                        agentBaseUrl = "http://localhost:8000"
                )

        // Then
        assertEquals("https://test.atlassian.net", config.confluenceBaseUrl)
        assertEquals("test-token", config.confluenceApiToken)
        assertEquals("test@example.com", config.confluenceUserEmail)
        assertEquals("TEST", config.confluenceSpaceKey)
        assertEquals("http://localhost:8000", config.agentBaseUrl)
        assertEquals(8081, config.indexerPort)
    }

    @Test
    fun `should use default indexer port`() {
        val config =
                AppConfig(
                        confluenceBaseUrl = "https://test.atlassian.net",
                        confluenceApiToken = "token",
                        confluenceUserEmail = "email",
                        confluenceSpaceKey = "SPACE",
                        agentBaseUrl = "http://localhost:8000"
                )

        assertEquals(8081, config.indexerPort)
    }
}
