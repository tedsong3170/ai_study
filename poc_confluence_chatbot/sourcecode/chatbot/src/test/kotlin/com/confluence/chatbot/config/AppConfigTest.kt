package com.confluence.chatbot.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AppConfigTest {

    @Test
    fun `should create config with default values`() {
        val config = AppConfig(agentBaseUrl = "http://localhost:8000")

        assertEquals("http://localhost:8000", config.agentBaseUrl)
        assertEquals(8080, config.chatbotPort)
        assertEquals(5, config.maxChatHistoryTurns)
    }

    @Test
    fun `should create config with custom values`() {
        val config =
                AppConfig(
                        agentBaseUrl = "http://agent:8000",
                        chatbotPort = 9090,
                        maxChatHistoryTurns = 10
                )

        assertEquals("http://agent:8000", config.agentBaseUrl)
        assertEquals(9090, config.chatbotPort)
        assertEquals(10, config.maxChatHistoryTurns)
    }
}
