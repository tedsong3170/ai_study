package com.stock.chat.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final StockTools stockTools;

    private StockAssistant assistant;

    @PostConstruct
    public void init() {
        assistant = AiServices.builder(StockAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(stockTools)
                .build();
    }

    public String chat(String message) {
        return assistant.chat(message);
    }
}
