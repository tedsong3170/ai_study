package com.stock.chat.service;

import com.stock.chat.client.KisApiClient;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockTools {

    private final KisApiClient kisApiClient;
    private final StockMasterService stockMasterService;

    @Tool("가격 급등락 종목 조회 - 오늘 가격이 급등하거나 급락한 종목 목록을 반환합니다")
    public String getPriceSurgeStocks() {
        return kisApiClient.getPriceSurgeStocks();
    }

    @Tool("거래량 급등락 종목 조회 - 오늘 거래량이 급등하거나 급락한 종목 목록을 반환합니다")
    public String getVolumeSurgeStocks() {
        return kisApiClient.getVolumeSurgeStocks();
    }

    @Tool("분봉 데이터 조회 - 특정 종목의 분봉 차트 데이터를 반환합니다. 종목명(삼성전자) 또는 종목코드(005930) 모두 사용 가능합니다.")
    public String getMinuteCandle(String stockNameOrCode) {
        String stockCode = stockMasterService.resolveStockCode(stockNameOrCode);
        if (stockCode == null) {
            return "종목을 찾을 수 없습니다: " + stockNameOrCode + ". 정확한 종목명이나 6자리 종목코드를 입력해주세요.";
        }
        return kisApiClient.getMinuteCandle(stockCode);
    }

    @Tool("종목 상세 정보 조회 - 특정 종목의 현재가, 거래량, 시가총액 등 상세 정보를 반환합니다. 종목명(삼성전자) 또는 종목코드(005930) 모두 사용 가능합니다.")
    public String getStockInfo(String stockNameOrCode) {
        String stockCode = stockMasterService.resolveStockCode(stockNameOrCode);
        if (stockCode == null) {
            return "종목을 찾을 수 없습니다: " + stockNameOrCode + ". 정확한 종목명이나 6자리 종목코드를 입력해주세요.";
        }
        return kisApiClient.getStockInfo(stockCode);
    }
}
