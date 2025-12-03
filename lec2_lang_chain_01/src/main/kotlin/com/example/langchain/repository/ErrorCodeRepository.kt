package com.example.langchain.repository

import com.example.langchain.domain.ErrorCode

/**
 * 에러 코드 저장소 인터페이스
 */
interface ErrorCodeRepository {
    /**
     * 모든 에러 코드 조회
     */
    fun findAll(): List<ErrorCode>

    /**
     * 특정 코드로 에러 조회
     */
    fun findByCode(code: String): ErrorCode?

    /**
     * 카테고리별 에러 코드 조회
     */
    fun findByCategory(category: String): List<ErrorCode>
}
