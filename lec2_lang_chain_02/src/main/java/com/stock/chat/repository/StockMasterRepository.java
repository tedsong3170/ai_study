package com.stock.chat.repository;

import com.stock.chat.entity.StockMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockMasterRepository extends JpaRepository<StockMaster, String> {

    /**
     * 정확한 종목명으로 검색
     */
    Optional<StockMaster> findByStockName(String stockName);

    /**
     * 종목명에 키워드가 포함된 종목 검색
     */
    List<StockMaster> findByStockNameContaining(String keyword);

    /**
     * 시장 유형별 조회
     */
    List<StockMaster> findByMarketType(String marketType);
}
