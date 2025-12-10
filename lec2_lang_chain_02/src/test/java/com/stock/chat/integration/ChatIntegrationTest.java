package com.stock.chat.integration;

import com.stock.chat.client.KisApiClient;
import com.stock.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class ChatIntegrationTest {

    @Autowired
    private ChatService chatService;

    @SpyBean
    private KisApiClient kisApiClient;

    @BeforeEach
    void setUp() {
        // Mock KIS API responses to avoid real API calls
        doReturn("삼성전자: 75000원 (5.2%)\nSK하이닉스: 180000원 (3.1%)")
                .when(kisApiClient).getPriceSurgeStocks();
        doReturn("카카오: 55000원 (2.5%)\n네이버: 220000원 (1.8%)")
                .when(kisApiClient).getVolumeSurgeStocks();
        doReturn("분봉 데이터:\n100000 - 현재가: 75000, 시가: 74500, 고가: 75500, 저가: 74000, 거래량: 10000")
                .when(kisApiClient).getMinuteCandle(anyString());
        doReturn("종목명: 삼성전자\n현재가: 75000원\n전일대비: 1000원 (1.35%)")
                .when(kisApiClient).getStockInfo(anyString());
    }

    @Test
    void shouldCallPriceSurgeApiForPriceSurgeQuery() {
        // 6.1 "가격 급등 종목 알려줘" 질의에 대해 가격 급등락 API 호출
        String response = chatService.chat("가격 급등 종목 알려줘");

        assertThat(response).isNotNull();
        verify(kisApiClient, atLeastOnce()).getPriceSurgeStocks();
    }

    @Test
    void shouldCallVolumeSurgeApiForVolumeSurgeQuery() {
        // 6.2 "거래량 급등 종목 알려줘" 질의에 대해 거래량 급등락 API 호출
        String response = chatService.chat("거래량 급등 종목 알려줘");

        assertThat(response).isNotNull();
        verify(kisApiClient, atLeastOnce()).getVolumeSurgeStocks();
    }

    @Test
    void shouldCallMinuteCandleApiForMinuteCandleQuery() {
        // 6.3 "삼성전자 분봉 데이터 보여줘" 질의에 대해 분봉 API 호출
        String response = chatService.chat("삼성전자 분봉 데이터 보여줘");

        assertThat(response).isNotNull();
        verify(kisApiClient, atLeastOnce()).getMinuteCandle(anyString());
    }

    @Test
    void shouldCallStockInfoApiForStockInfoQuery() {
        // 6.4 "삼성전자 정보 알려줘" 질의에 대해 종목 상세 API 호출
        String response = chatService.chat("삼성전자 정보 알려줘");

        assertThat(response).isNotNull();
        verify(kisApiClient, atLeastOnce()).getStockInfo(anyString());
    }
}
