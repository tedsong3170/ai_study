package com.stock.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_master")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMaster {

    @Id
    private String stockCode; // 종목코드 (005930)

    private String stockName; // 종목명 (삼성전자)

    private String marketType; // 시장구분 (KOSPI/KOSDAQ)
}
