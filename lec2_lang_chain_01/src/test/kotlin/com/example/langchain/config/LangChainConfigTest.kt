package com.example.langchain.config

import dev.langchain4j.model.chat.ChatLanguageModel
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest
class LangChainConfigTest {

    @Autowired(required = false)
    private lateinit var chatLanguageModel: ChatLanguageModel

    @Test
    fun `should create ChatLanguageModel bean`() {
        // Then
        assertNotNull(chatLanguageModel, "ChatLanguageModel bean should be created")
    }

    @Test
    fun `should generate response from ChatLanguageModel`() {
        // Given
        val prompt = "Hello, can you respond?"

        // When
        val response = chatLanguageModel.generate(prompt)

        // Then
        assertNotNull(response, "Response should not be null")
        println("ChatLanguageModel response: $response")
    }
}
