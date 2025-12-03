package com.example.langchain.config

import com.example.langchain.service.ErrorCodeAiService
import com.example.langchain.service.ErrorCodeTools
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.service.AiServices
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
    
    /**
     * ErrorCodeAiService Bean 등록
     * LangChain4j가 자동으로 구현체를 생성하고 Tools를 연동
     */
    @Bean
    fun errorCodeAiService(
        chatLanguageModel: ChatLanguageModel,
        errorCodeTools: ErrorCodeTools
    ): ErrorCodeAiService {
        return AiServices.builder(ErrorCodeAiService::class.java)
            .chatLanguageModel(chatLanguageModel)
            .tools(errorCodeTools)
            .build()
    }
}
