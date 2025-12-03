package com.example.langchain.repository

import com.example.langchain.config.DataInitializer
import com.example.langchain.domain.ErrorCode
import com.example.langchain.mapper.ErrorCodeMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * ErrorCodeRepository 통합 테스트
 * JPA Repository 구현체 검증
 */
@DataJpaTest
@Import(ErrorCodeRepositoryImpl::class, ErrorCodeMapper::class, DataInitializer::class)
@ActiveProfiles("test")
class ErrorCodeRepositoryImplTest {
    
    @Autowired
    private lateinit var errorCodeRepository: ErrorCodeRepository
    
    @Test
    @DisplayName("모든 에러 코드 조회")
    fun findAll() {
        // when
        val result = errorCodeRepository.findAll()
        
        // then
        assertThat(result).isNotEmpty
        assertThat(result).hasSize(10)
        assertThat(result).allMatch { it is ErrorCode }
    }
    
    @Test
    @DisplayName("특정 코드로 에러 조회 - 존재하는 경우")
    fun findByCode_exists() {
        // when
        val result = errorCodeRepository.findByCode("ERR001")
        
        // then
        assertThat(result).isNotNull
        assertThat(result?.code).isEqualTo("ERR001")
        assertThat(result?.message).isEqualTo("Invalid credentials")
        assertThat(result?.category).isEqualTo("AUTHENTICATION")
    }
    
    @Test
    @DisplayName("특정 코드로 에러 조회 - 존재하지 않는 경우")
    fun findByCode_notExists() {
        // when
        val result = errorCodeRepository.findByCode("ERR999")
        
        // then
        assertThat(result).isNull()
    }
    
    @Test
    @DisplayName("카테고리별 에러 코드 조회 - AUTHENTICATION")
    fun findByCategory_authentication() {
        // when
        val result = errorCodeRepository.findByCategory("AUTHENTICATION")
        
        // then
        assertThat(result).hasSize(3)
        assertThat(result).allMatch { it.category == "AUTHENTICATION" }
        assertThat(result.map { it.code }).containsExactlyInAnyOrder(
            "ERR001", "ERR002", "ERR003"
        )
    }
    
    @Test
    @DisplayName("카테고리별 에러 코드 조회 - VALIDATION")
    fun findByCategory_validation() {
        // when
        val result = errorCodeRepository.findByCategory("VALIDATION")
        
        // then
        assertThat(result).hasSize(3)
        assertThat(result).allMatch { it.category == "VALIDATION" }
    }
    
    @Test
    @DisplayName("카테고리별 에러 코드 조회 - 결과 없음")
    fun findByCategory_noResults() {
        // when
        val result = errorCodeRepository.findByCategory("NONEXISTENT")
        
        // then
        assertThat(result).isEmpty()
    }
    
    @Test
    @DisplayName("도메인 객체 검증 - 순수 Kotlin 객체")
    fun verifyDomainObject() {
        // when
        val result = errorCodeRepository.findByCode("ERR001")
        
        // then
        assertThat(result).isNotNull
        assertThat(result).isInstanceOf(ErrorCode::class.java)
        // JPA 어노테이션이 없는 순수 도메인 객체 확인
        assertThat(result!!::class.java.annotations)
            .noneMatch { it.annotationClass.simpleName == "Entity" }
    }
}
