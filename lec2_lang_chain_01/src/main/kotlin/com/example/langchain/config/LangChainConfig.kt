package com.example.langchain.config

import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * LangChain4j 설정
 */
@Configuration
class LangChainConfig {

    @Value("\${langchain.ollama.base-url}")
    private lateinit var baseUrl: String

    @Value("\${langchain.ollama.model-name}")
    private lateinit var modelName: String

    @Value("\${langchain.ollama.timeout}")
    private lateinit var timeout: String

    @Bean
    fun chatLanguageModel(): ChatLanguageModel {
        return OllamaChatModel.builder()
            .baseUrl(baseUrl)
            .modelName(modelName)
            .timeout(Duration.parse("PT$timeout"))
            .build()
    }
}
