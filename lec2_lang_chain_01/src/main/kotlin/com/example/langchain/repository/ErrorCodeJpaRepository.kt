package com.example.langchain.repository

import com.example.langchain.entity.ErrorCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * ErrorCodeEntity JPA Repository (Persistence Layer)
 */
interface ErrorCodeJpaRepository : JpaRepository<ErrorCodeEntity, String> {
    
    /**
     * 카테고리로 에러 코드 조회
     */
    fun findByCategory(category: String): List<ErrorCodeEntity>
    
    /**
     * 코드 또는 메시지에 키워드가 포함된 에러 코드 검색
     */
    fun findByCodeContainingOrMessageContaining(code: String, message: String): List<ErrorCodeEntity>
}
