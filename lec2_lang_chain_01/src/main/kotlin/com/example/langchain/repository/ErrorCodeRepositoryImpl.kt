package com.example.langchain.repository

import com.example.langchain.domain.ErrorCode
import com.example.langchain.mapper.ErrorCodeMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

/**
 * ErrorCodeRepository 구현체 (도메인 인터페이스 구현)
 * JPA Repository를 사용하여 데이터 조회 후 도메인 객체로 변환
 */
@Repository
@Primary
class ErrorCodeRepositoryImpl(
    private val jpaRepository: ErrorCodeJpaRepository,
    private val mapper: ErrorCodeMapper
) : ErrorCodeRepository {
    
    override fun findAll(): List<ErrorCode> {
        return mapper.toDomainList(jpaRepository.findAll())
    }
    
    override fun findByCode(code: String): ErrorCode? {
        return jpaRepository.findById(code)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun findByCategory(category: String): List<ErrorCode> {
        return mapper.toDomainList(jpaRepository.findByCategory(category))
    }
}
