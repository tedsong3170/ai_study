package com.example.langchain.controller

import com.example.langchain.domain.ErrorCode
import com.example.langchain.repository.ErrorCodeRepository
import com.example.langchain.service.ErrorCodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 에러 코드 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/error-codes")
class ErrorCodeController(
    private val errorCodeRepository: ErrorCodeRepository,
    private val errorCodeService: ErrorCodeService
) {

    /**
     * 모든 에러 코드 조회
     */
    @GetMapping
    fun getAllErrorCodes(): ResponseEntity<List<ErrorCode>> {
        val errorCodes = errorCodeRepository.findAll()
        return ResponseEntity.ok(errorCodes)
    }

    /**
     * 특정 에러 코드 조회
     */
    @GetMapping("/{code}")
    fun getErrorCodeByCode(@PathVariable code: String): ResponseEntity<ErrorCode> {
        val errorCode = errorCodeRepository.findByCode(code)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(errorCode)
    }

    /**
     * AI 질의응답
     */
    @PostMapping("/query")
    fun queryErrorCode(@RequestBody request: QueryRequest): ResponseEntity<QueryResponse> {
        if (request.query.isNullOrBlank()) {
            return ResponseEntity.badRequest().build()
        }

        val answer = errorCodeService.queryErrorCode(request.query)
        return ResponseEntity.ok(QueryResponse(answer))
    }
}

/**
 * 질의 요청 DTO
 */
data class QueryRequest(
    val query: String?
)

/**
 * 질의 응답 DTO
 */
data class QueryResponse(
    val answer: String
)
