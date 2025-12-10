package com.stock.chat.client;

import com.stock.chat.config.KisApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class KisApiClientTest {

    @Autowired
    private KisApiClient kisApiClient;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private KisApiProperties properties;

    @BeforeEach
    void setUp() {
        // Mock token response
        Map<String, Object> tokenResponse = Map.of("access_token", "test-token-12345");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(tokenResponse);
    }

    @Test
    void shouldBeRegisteredAsSpringBean() {
        // 3.1 KisApiClient가 Spring Bean으로 등록됨
        assertThat(kisApiClient).isNotNull();
    }

    @Test
    void shouldGetAccessToken() {
        // 3.2 KIS API 인증 토큰 발급 기능
        String token = kisApiClient.getAccessToken();
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).isEqualTo("test-token-12345");
    }

    @Test
    void shouldGetPriceSurgeStocks() {
        // 4.1 가격 급등락 종목 조회 기능
        Map<String, Object> stockResponse = Map.of(
                "output", List.of(
                        Map.of("hts_kor_isnm", "삼성전자", "stck_prpr", "75000", "prdy_ctrt", "5.2"),
                        Map.of("hts_kor_isnm", "SK하이닉스", "stck_prpr", "180000", "prdy_ctrt", "3.1")
                )
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(stockResponse));

        String result = kisApiClient.getPriceSurgeStocks();
        assertThat(result).isNotNull();
        assertThat(result).contains("삼성전자");
    }

    @Test
    void shouldGetVolumeSurgeStocks() {
        // 4.2 거래량 급등락 종목 조회 기능
        Map<String, Object> stockResponse = Map.of(
                "output", List.of(
                        Map.of("hts_kor_isnm", "카카오", "stck_prpr", "55000", "prdy_ctrt", "2.5"),
                        Map.of("hts_kor_isnm", "네이버", "stck_prpr", "220000", "prdy_ctrt", "1.8")
                )
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(stockResponse));

        String result = kisApiClient.getVolumeSurgeStocks();
        assertThat(result).isNotNull();
        assertThat(result).contains("카카오");
    }

    @Test
    void shouldGetMinuteCandle() {
        // 4.3 분봉 데이터 조회 기능
        Map<String, Object> candleResponse = Map.of(
                "output2", List.of(
                        Map.of("stck_cntg_hour", "100000", "stck_prpr", "75000", "stck_oprc", "74500", "stck_hgpr", "75500", "stck_lwpr", "74000", "cntg_vol", "10000"),
                        Map.of("stck_cntg_hour", "100100", "stck_prpr", "75200", "stck_oprc", "75000", "stck_hgpr", "75300", "stck_lwpr", "74900", "cntg_vol", "8000")
                )
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(candleResponse));

        String result = kisApiClient.getMinuteCandle("005930");
        assertThat(result).isNotNull();
        assertThat(result).contains("75000");
    }

    @Test
    void shouldGetStockInfo() {
        // 4.4 종목 상세 데이터 조회 기능
        Map<String, Object> stockInfoResponse = Map.of(
                "output", Map.of(
                        "hts_kor_isnm", "삼성전자",
                        "stck_prpr", "75000",
                        "prdy_vrss", "1000",
                        "prdy_ctrt", "1.35",
                        "acml_vol", "15000000",
                        "acml_tr_pbmn", "1125000000000",
                        "hts_avls", "450000000000000",
                        "per", "12.5",
                        "pbr", "1.2"
                )
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(stockInfoResponse));

        String result = kisApiClient.getStockInfo("005930");
        assertThat(result).isNotNull();
        assertThat(result).contains("삼성전자");
        assertThat(result).contains("75000");
    }
}
