package com.stock.chat.service;

import com.stock.chat.entity.StockMaster;
import com.stock.chat.repository.StockMasterRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMasterService {

    private final StockMasterRepository stockMasterRepository;

    private static final String KOSPI_URL = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";
    private static final String KOSDAQ_URL = "https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip";

    @PostConstruct
    public void init() {
        if (stockMasterRepository.count() == 0) {
            log.info("Stock master data is empty. Downloading master files...");
            downloadAndSaveAllMasterFiles();
        } else {
            log.info("Stock master data already exists. {} stocks loaded.", stockMasterRepository.count());
        }
    }

    /**
     * 종목명 또는 종목코드를 종목코드로 변환
     */
    public String resolveStockCode(String stockNameOrCode) {
        if (stockNameOrCode == null || stockNameOrCode.isBlank()) {
            return null;
        }

        String input = stockNameOrCode.trim();

        // 숫자로만 구성되어 있으면 종목코드로 간주
        if (input.matches("\\d+")) {
            // 6자리로 패딩
            return String.format("%06d", Integer.parseInt(input));
        }

        // DB에서 종목명으로 검색
        Optional<StockMaster> stock = stockMasterRepository.findByStockName(input);
        if (stock.isPresent()) {
            log.info("Found stock: {} -> {}", input, stock.get().getStockCode());
            return stock.get().getStockCode();
        }

        // 부분 일치 검색 시도
        List<StockMaster> partialMatches = stockMasterRepository.findByStockNameContaining(input);
        if (!partialMatches.isEmpty()) {
            StockMaster matched = partialMatches.get(0);
            log.info("Found partial match: {} -> {} ({})", input, matched.getStockCode(), matched.getStockName());
            return matched.getStockCode();
        }

        log.warn("Stock not found: {}", input);
        return null;
    }

    /**
     * 코스피/코스닥 마스터파일 다운로드 및 저장
     */
    public void downloadAndSaveAllMasterFiles() {
        try {
            List<StockMaster> allStocks = new ArrayList<>();

            log.info("Downloading KOSPI master file...");
            allStocks.addAll(downloadAndParseMasterFile(KOSPI_URL, "KOSPI"));

            log.info("Downloading KOSDAQ master file...");
            allStocks.addAll(downloadAndParseMasterFile(KOSDAQ_URL, "KOSDAQ"));

            if (!allStocks.isEmpty()) {
                stockMasterRepository.saveAll(allStocks);
                log.info("Saved {} stocks to database.", allStocks.size());
            }
        } catch (Exception e) {
            log.error("Failed to download master files: {}", e.getMessage(), e);
        }
    }

    private List<StockMaster> downloadAndParseMasterFile(String urlStr, String marketType) {
        List<StockMaster> stocks = new ArrayList<>();

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            if (conn.getResponseCode() != 200) {
                log.error("Failed to download: {} - Response: {}", urlStr, conn.getResponseCode());
                return stocks;
            }

            // ZIP 파일을 임시 파일로 저장
            Path tempZip = Files.createTempFile("master_", ".zip");
            try (InputStream is = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(tempZip.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // ZIP 압축 해제 및 파싱
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZip.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".mst")) {
                        stocks = parseMstContent(zis, marketType);
                    }
                    zis.closeEntry();
                }
            }

            Files.deleteIfExists(tempZip);
            log.info("Parsed {} stocks from {}", stocks.size(), marketType);

        } catch (Exception e) {
            log.error("Error downloading/parsing master file: {}", e.getMessage(), e);
        }

        return stocks;
    }

    /**
     * MST 파일 파싱 - KIS 마스터파일 형식
     * 형식: 고정길이 레코드 (종목코드: 9자리, 종목명: 40자리 등)
     */
    private List<StockMaster> parseMstContent(InputStream inputStream, String marketType) throws IOException {
        List<StockMaster> stocks = new ArrayList<>();

        // CP949(EUC-KR) 인코딩으로 읽기
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("CP949")));

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                if (line.length() < 50)
                    continue;

                // KIS 마스터파일 형식 파싱
                // 단축코드: 0-9 (9자리), 표준코드: 9-21 (12자리), 한글명: 21-61 (40자리)
                String shortCode = line.substring(0, 9).trim();
                String stockName = line.substring(21, Math.min(61, line.length())).trim();

                // 6자리 종목코드 추출 (앞의 A 또는 Q 제거)
                String stockCode = shortCode;
                if (shortCode.length() > 6) {
                    // 보통 앞에 'A' 또는 시장 구분자가 붙음
                    stockCode = shortCode.substring(shortCode.length() - 6);
                }

                if (stockCode.matches("\\d{6}") && !stockName.isEmpty()) {
                    stocks.add(StockMaster.builder()
                            .stockCode(stockCode)
                            .stockName(stockName)
                            .marketType(marketType)
                            .build());
                }
            } catch (Exception e) {
                // 파싱 오류 무시 (일부 라인 형식 다를 수 있음)
            }
        }

        return stocks;
    }

    /**
     * 수동으로 마스터 데이터 갱신
     */
    public void refreshMasterData() {
        log.info("Refreshing stock master data...");
        stockMasterRepository.deleteAll();
        downloadAndSaveAllMasterFiles();
    }

    /**
     * 종목 검색 (부분 일치)
     */
    public List<StockMaster> searchStocks(String keyword) {
        return stockMasterRepository.findByStockNameContaining(keyword);
    }
}
