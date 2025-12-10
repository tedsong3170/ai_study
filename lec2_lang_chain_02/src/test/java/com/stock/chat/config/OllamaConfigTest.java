package com.stock.chat.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OllamaConfigTest {

    @Autowired(required = false)
    private ChatLanguageModel chatLanguageModel;

    @Test
    void shouldRegisterOllamaChatModelAsSpringBean() {
        // 5.1 Ollama ChatModel이 Spring Bean으로 등록됨
        assertThat(chatLanguageModel).isNotNull();
    }
}
