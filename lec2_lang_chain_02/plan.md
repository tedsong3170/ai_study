# Test Plan - 자연어 주식 정보 조회 서비스

## Phase 1: 프로젝트 기본 설정

- [x] 1.1 Spring Boot 애플리케이션이 정상적으로 시작되는지 확인

## Phase 2: Chat API 엔드포인트

- [x] 2.1 POST /api/chat 엔드포인트가 존재하고 200 응답을 반환
- [x] 2.2 요청 본문에 message가 없으면 400 에러 반환
- [x] 2.3 빈 message로 요청하면 400 에러 반환

## Phase 3: KIS API Client 기본 구조

- [x] 3.1 KisApiClient가 Spring Bean으로 등록됨
- [x] 3.2 KIS API 인증 토큰 발급 기능

## Phase 4: 주식 정보 조회 기능 (KIS API)

- [x] 4.1 가격 급등락 종목 조회 기능
- [x] 4.2 거래량 급등락 종목 조회 기능
- [x] 4.3 분봉 데이터 조회 기능
- [x] 4.4 종목 상세 데이터 조회 기능

## Phase 5: LangChain4j 통합

- [x] 5.1 Ollama ChatModel이 Spring Bean으로 등록됨
- [x] 5.2 StockTools가 LangChain4j Tool로 등록됨
- [x] 5.3 ChatService가 자연어 질의를 처리하고 응답 반환

## Phase 6: 통합 테스트

- [x] 6.1 "가격 급등 종목 알려줘" 질의에 대해 가격 급등락 API 호출
- [x] 6.2 "거래량 급등 종목 알려줘" 질의에 대해 거래량 급등락 API 호출
- [x] 6.3 "삼성전자 분봉 데이터 보여줘" 질의에 대해 분봉 API 호출
- [x] 6.4 "삼성전자 정보 알려줘" 질의에 대해 종목 상세 API 호출
