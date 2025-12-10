package com.stock.chat.service;

import org.junit.jupiter.api.BeforeEach;
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
class StockToolsTest {

        @Autowired
        private StockTools stockTools;

        @MockBean
        private RestTemplate restTemplate;

        @BeforeEach
        void setUp() {
                // Mock token response
                Map<String, Object> tokenResponse = Map.of("access_token", "test-token");
                when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                                .thenReturn(tokenResponse);

                // Mock stock responses
                Map<String, Object> stockResponse = Map.of(
                                "output", List.of(
                                                Map.of("hts_kor_isnm", "삼성전자", "stck_prpr", "75000", "prdy_ctrt",
                                                                "5.2")));
                when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                                .thenReturn(ResponseEntity.ok(stockResponse));
        }

        @Test
        void shouldBeRegisteredAsSpringBean() {
                // 5.2 StockTools가 LangChain4j Tool로 등록됨
                assertThat(stockTools).isNotNull();
        }

        @Test
        void shouldHavePriceSurgeTool() {
                String result = stockTools.getPriceSurgeStocks();
                assertThat(result).isNotNull();
                assertThat(result).contains("삼성전자");
        }

        @Test
        void shouldHaveVolumeSurgeTool() {
                String result = stockTools.getVolumeSurgeStocks();
                assertThat(result).isNotNull();
        }

        @Test
        void shouldHaveMinuteCandleTool() {
                Map<String, Object> candleResponse = Map.of(
                                "output2", List.of(
                                                Map.of("stck_cntg_hour", "100000", "stck_prpr", "75000", "stck_oprc",
                                                                "74500",
                                                                "stck_hgpr", "75500", "stck_lwpr", "74000", "cntg_vol",
                                                                "10000")));
                when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                                .thenReturn(ResponseEntity.ok(candleResponse));

                String result = stockTools.getMinuteCandle("005930");
                assertThat(result).isNotNull();
        }

        @Test
        void shouldHaveStockInfoTool() {
                Map<String, Object> stockInfoResponse = Map.of(
                                "output", Map.of(
                                                "hts_kor_isnm", "삼성전자",
                                                "stck_prpr", "75000"));
                when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                                .thenReturn(ResponseEntity.ok(stockInfoResponse));

                String result = stockTools.getStockInfo("005930");
                assertThat(result).isNotNull();
                assertThat(result).contains("삼성전자");
        }
}
