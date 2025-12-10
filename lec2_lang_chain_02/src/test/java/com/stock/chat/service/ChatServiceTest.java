package com.stock.chat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class ChatServiceTest {

    @Autowired(required = false)
    private ChatService chatService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void shouldBeRegisteredAsSpringBean() {
        // 5.3 ChatService가 자연어 질의를 처리하고 응답 반환
        assertThat(chatService).isNotNull();
    }

    @Test
    void shouldProcessUserMessage() {
        // Mock token response
        Map<String, Object> tokenResponse = Map.of("access_token", "test-token");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(tokenResponse);

        // Mock stock responses
        Map<String, Object> stockResponse = Map.of(
                "output", List.of(
                        Map.of("hts_kor_isnm", "삼성전자", "stck_prpr", "75000", "prdy_ctrt", "5.2")));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(stockResponse));

        String response = chatService.chat("안녕하세요");
        assertThat(response).isNotNull();
        assertThat(response).isNotEmpty();
    }
}
