package com.stock.chat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.chat.config.KisApiProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisApiClient {

    private final RestTemplate restTemplate;
    private final KisApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private LocalDateTime tokenExpirationTime;

    // 토큰 유효 시간 (23시간 - KIS 토큰은 24시간 만료이므로 여유 확보)
    private static final int TOKEN_VALID_HOURS = 23;
    private static final String TOKEN_FILE_PATH = "kis_token.json";

    @PostConstruct
    public void init() {
        loadTokenFromFile();
    }

    public String getAccessToken() {
        if (isTokenValid()) {
            log.debug("Using cached access token (expires at: {})", tokenExpirationTime);
            return accessToken;
        }

        log.info("Requesting new access token from KIS API...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "grant_type", "client_credentials",
                "appkey", properties.getAppKey(),
                "appsecret", properties.getAppSecret());

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    properties.getBaseUrl() + "/oauth2/tokenP",
                    request,
                    Map.class);

            log.debug("Token response: {}", response);

            if (response != null && response.containsKey("access_token")) {
                accessToken = (String) response.get("access_token");
                tokenExpirationTime = LocalDateTime.now().plusHours(TOKEN_VALID_HOURS);
                log.info("Access token obtained successfully (valid until: {})", tokenExpirationTime);
                saveTokenToFile();
            } else {
                log.error("Failed to get access token. Response: {}", response);
            }
        } catch (Exception e) {
            log.error("Error requesting access token: {}", e.getMessage(), e);
        }

        return accessToken;
    }

    private boolean isTokenValid() {
        return accessToken != null
                && tokenExpirationTime != null
                && LocalDateTime.now().isBefore(tokenExpirationTime);
    }

    private void saveTokenToFile() {
        try {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("access_token", accessToken);
            tokenData.put("expiration_time", tokenExpirationTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            objectMapper.writeValue(new File(TOKEN_FILE_PATH), tokenData);
            log.info("Token saved to file: {}", TOKEN_FILE_PATH);
        } catch (IOException e) {
            log.warn("Failed to save token to file: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTokenFromFile() {
        File tokenFile = new File(TOKEN_FILE_PATH);
        if (!tokenFile.exists()) {
            log.debug("Token file not found, will request new token");
            return;
        }

        try {
            Map<String, String> tokenData = objectMapper.readValue(tokenFile, Map.class);
            String savedToken = tokenData.get("access_token");
            String expirationStr = tokenData.get("expiration_time");

            if (savedToken != null && expirationStr != null) {
                LocalDateTime expiration = LocalDateTime.parse(expirationStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (LocalDateTime.now().isBefore(expiration)) {
                    this.accessToken = savedToken;
                    this.tokenExpirationTime = expiration;
                    log.info("Token loaded from file (valid until: {})", tokenExpirationTime);
                } else {
                    log.info("Saved token has expired, will request new token");
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load token from file: {}", e.getMessage());
        }
    }

    public String getPriceSurgeStocks() {
        HttpHeaders headers = createAuthHeaders();
        headers.set("tr_id", "FHPST01700000");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = properties.getBaseUrl() + "/uapi/domestic-stock/v1/ranking/fluctuation" +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20170" +
                "&fid_input_iscd=0000" +
                "&fid_rank_sort_cls_code=0" +
                "&fid_input_cnt_1=0" +
                "&fid_prc_cls_code=0" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=" +
                "&fid_trgt_cls_code=0" +
                "&fid_trgt_exls_cls_code=0" +
                "&fid_div_cls_code=0" +
                "&fid_rsfl_rate1=" +
                "&fid_rsfl_rate2=";

        log.debug("Calling getPriceSurgeStocks API: {}", url);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class).getBody();

            log.debug("getPriceSurgeStocks response: {}", response);
            return formatStockResponse(response);
        } catch (Exception e) {
            log.error("Error calling getPriceSurgeStocks: {}", e.getMessage(), e);
            return "가격 급등락 API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    public String getVolumeSurgeStocks() {
        HttpHeaders headers = createAuthHeaders();
        headers.set("tr_id", "FHPST01710000");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = properties.getBaseUrl() + "/uapi/domestic-stock/v1/ranking/volume" +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_cond_scr_div_code=20171" +
                "&fid_input_iscd=0000" +
                "&fid_rank_sort_cls_code=0" +
                "&fid_div_cls_code=0" +
                "&fid_blng_cls_code=0" +
                "&fid_trgt_cls_code=111111111" +
                "&fid_trgt_exls_cls_code=000000" +
                "&fid_input_price_1=" +
                "&fid_input_price_2=" +
                "&fid_vol_cnt=" +
                "&fid_input_date_1=";

        log.debug("Calling getVolumeSurgeStocks API: {}", url);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class).getBody();

            log.debug("getVolumeSurgeStocks response: {}", response);
            return formatStockResponse(response);
        } catch (Exception e) {
            log.error("Error calling getVolumeSurgeStocks: {}", e.getMessage(), e);
            return "거래량 급등락 API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    public String getMinuteCandle(String stockCode) {
        HttpHeaders headers = createAuthHeaders();
        headers.set("tr_id", "FHKST03010200");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 현재 시간을 HHMMSS 형식으로 (장 시간 내에서 조회)
        String currentTime = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));

        String url = properties.getBaseUrl() + "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice" +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + stockCode +
                "&fid_input_hour_1=" + currentTime +
                "&fid_pw_data_incu_yn=N";

        log.info("Calling getMinuteCandle API for stockCode {}: {}", stockCode, url);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class).getBody();

            log.info("getMinuteCandle response: {}", response);
            return formatCandleResponse(response);
        } catch (Exception e) {
            log.error("Error calling getMinuteCandle: {}", e.getMessage(), e);
            return "분봉 데이터 API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    public String getStockInfo(String stockCode) {
        HttpHeaders headers = createAuthHeaders();
        headers.set("tr_id", "FHKST01010100");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = properties.getBaseUrl() + "/uapi/domestic-stock/v1/quotations/inquire-price" +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + stockCode;

        log.info("Calling getStockInfo API for stockCode {}: {}", stockCode, url);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class).getBody();

            log.info("getStockInfo response keys: {}", response != null ? response.keySet() : "null");
            log.debug("getStockInfo full response: {}", response);

            return formatStockInfoResponse(response);
        } catch (Exception e) {
            log.error("Error calling getStockInfo: {}", e.getMessage(), e);
            return "종목 정보 API 호출 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + getAccessToken());
        headers.set("appkey", properties.getAppKey());
        headers.set("appsecret", properties.getAppSecret());
        return headers;
    }

    @SuppressWarnings("unchecked")
    private String formatStockResponse(Map<String, Object> response) {
        if (response == null) {
            log.warn("Response is null");
            return "데이터를 가져올 수 없습니다. (응답 없음)";
        }

        log.debug("Response keys: {}", response.keySet());

        if (!response.containsKey("output")) {
            String rtCd = (String) response.get("rt_cd");
            String msg1 = (String) response.get("msg1");
            log.warn("No 'output' in response. rt_cd: {}, msg1: {}", rtCd, msg1);
            return String.format("데이터를 가져올 수 없습니다. (코드: %s, 메시지: %s)", rtCd, msg1);
        }

        List<Map<String, Object>> output = (List<Map<String, Object>>) response.get("output");
        if (output == null || output.isEmpty()) {
            return "조회된 데이터가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map<String, Object> stock : output) {
            if (count >= 10)
                break; // 최대 10개만 표시
            String name = (String) stock.getOrDefault("hts_kor_isnm", "");
            String price = (String) stock.getOrDefault("stck_prpr", "");
            String changeRate = (String) stock.getOrDefault("prdy_ctrt", "");
            if (!name.isEmpty()) {
                sb.append(String.format("%s: %s원 (%s%%)\n", name, price, changeRate));
                count++;
            }
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String formatCandleResponse(Map<String, Object> response) {
        if (response == null) {
            log.warn("Candle response is null");
            return "분봉 데이터를 가져올 수 없습니다. (응답 없음)";
        }

        log.debug("Candle response keys: {}", response.keySet());

        // output1에 종목 기본정보, output2에 분봉 데이터
        if (!response.containsKey("output2")) {
            String rtCd = (String) response.get("rt_cd");
            String msg1 = (String) response.get("msg1");
            log.warn("No 'output2' in candle response. rt_cd: {}, msg1: {}", rtCd, msg1);
            return String.format("분봉 데이터를 가져올 수 없습니다. (코드: %s, 메시지: %s)", rtCd, msg1);
        }

        List<Map<String, Object>> output = (List<Map<String, Object>>) response.get("output2");
        if (output == null || output.isEmpty()) {
            return "분봉 데이터가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("분봉 데이터:\n");

        int count = 0;
        for (Map<String, Object> candle : output) {
            if (count >= 10)
                break; // 최대 10개만 표시
            String time = (String) candle.getOrDefault("stck_cntg_hour", "");
            String price = (String) candle.getOrDefault("stck_prpr", "");
            String open = (String) candle.getOrDefault("stck_oprc", "");
            String high = (String) candle.getOrDefault("stck_hgpr", "");
            String low = (String) candle.getOrDefault("stck_lwpr", "");
            String volume = (String) candle.getOrDefault("cntg_vol", "");
            sb.append(String.format("%s - 현재가: %s, 시가: %s, 고가: %s, 저가: %s, 거래량: %s\n",
                    time, price, open, high, low, volume));
            count++;
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String formatStockInfoResponse(Map<String, Object> response) {
        if (response == null) {
            log.warn("StockInfo response is null");
            return "종목 정보를 가져올 수 없습니다. (응답 없음)";
        }

        log.debug("StockInfo response keys: {}", response.keySet());

        if (!response.containsKey("output")) {
            String rtCd = (String) response.get("rt_cd");
            String msg1 = (String) response.get("msg1");
            log.warn("No 'output' in stockInfo response. rt_cd: {}, msg1: {}", rtCd, msg1);
            return String.format("종목 정보를 가져올 수 없습니다. (코드: %s, 메시지: %s)", rtCd, msg1);
        }

        Map<String, Object> output = (Map<String, Object>) response.get("output");
        if (output == null || output.isEmpty()) {
            return "종목 정보가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();

        String name = (String) output.getOrDefault("hts_kor_isnm", "");
        String price = (String) output.getOrDefault("stck_prpr", "");
        String change = (String) output.getOrDefault("prdy_vrss", "");
        String changeRate = (String) output.getOrDefault("prdy_ctrt", "");
        String volume = (String) output.getOrDefault("acml_vol", "");
        String tradingValue = (String) output.getOrDefault("acml_tr_pbmn", "");
        String marketCap = (String) output.getOrDefault("hts_avls", "");
        String per = (String) output.getOrDefault("per", "");
        String pbr = (String) output.getOrDefault("pbr", "");

        sb.append(String.format("종목명: %s\n", name));
        sb.append(String.format("현재가: %s원\n", price));
        sb.append(String.format("전일대비: %s원 (%s%%)\n", change, changeRate));
        sb.append(String.format("거래량: %s주\n", volume));
        sb.append(String.format("거래대금: %s원\n", tradingValue));
        sb.append(String.format("시가총액: %s억원\n", marketCap));
        sb.append(String.format("PER: %s\n", per));
        sb.append(String.format("PBR: %s\n", pbr));

        return sb.toString();
    }
}
