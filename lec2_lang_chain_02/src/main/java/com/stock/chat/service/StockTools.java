package com.stock.chat.service;

import com.stock.chat.client.KisApiClient;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockTools {

    private final KisApiClient kisApiClient;

    @Tool("가격 급등락 종목 조회 - 오늘 가격이 급등하거나 급락한 종목 목록을 반환합니다")
    public String getPriceSurgeStocks() {
        return kisApiClient.getPriceSurgeStocks();
    }

    @Tool("거래량 급등락 종목 조회 - 오늘 거래량이 급등하거나 급락한 종목 목록을 반환합니다")
    public String getVolumeSurgeStocks() {
        return kisApiClient.getVolumeSurgeStocks();
    }

    @Tool("분봉 데이터 조회 - 특정 종목의 분봉 차트 데이터를 반환합니다")
    public String getMinuteCandle(String stockCode) {
        return kisApiClient.getMinuteCandle(stockCode);
    }

    @Tool("종목 상세 정보 조회 - 특정 종목의 현재가, 거래량, 시가총액 등 상세 정보를 반환합니다")
    public String getStockInfo(String stockCode) {
        return kisApiClient.getStockInfo(stockCode);
    }
}
