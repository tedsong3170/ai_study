package com.example.langchain.mapper

import com.example.langchain.domain.ErrorCode
import com.example.langchain.entity.ErrorCodeEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * ErrorCodeMapper 단위 테스트
 */
class ErrorCodeMapperTest {
    
    private val mapper = ErrorCodeMapper()
    
    @Test
    @DisplayName("Domain -> Entity 변환")
    fun toEntity() {
        // given
        val domain = ErrorCode(
            code = "TEST001",
            message = "Test message",
            description = "Test description",
            category = "TEST"
        )
        
        // when
        val entity = mapper.toEntity(domain)
        
        // then
        assertThat(entity.code).isEqualTo(domain.code)
        assertThat(entity.message).isEqualTo(domain.message)
        assertThat(entity.description).isEqualTo(domain.description)
        assertThat(entity.category).isEqualTo(domain.category)
    }
    
    @Test
    @DisplayName("Entity -> Domain 변환")
    fun toDomain() {
        // given
        val entity = ErrorCodeEntity(
            code = "TEST001",
            message = "Test message",
            description = "Test description",
            category = "TEST"
        )
        
        // when
        val domain = mapper.toDomain(entity)
        
        // then
        assertThat(domain.code).isEqualTo(entity.code)
        assertThat(domain.message).isEqualTo(entity.message)
        assertThat(domain.description).isEqualTo(entity.description)
        assertThat(domain.category).isEqualTo(entity.category)
    }
    
    @Test
    @DisplayName("Entity 리스트 -> Domain 리스트 변환")
    fun toDomainList() {
        // given
        val entities = listOf(
            ErrorCodeEntity("ERR001", "Message 1", "Description 1", "CAT1"),
            ErrorCodeEntity("ERR002", "Message 2", "Description 2", "CAT2")
        )
        
        // when
        val domains = mapper.toDomainList(entities)
        
        // then
        assertThat(domains).hasSize(2)
        assertThat(domains[0].code).isEqualTo("ERR001")
        assertThat(domains[1].code).isEqualTo("ERR002")
    }
    
    @Test
    @DisplayName("양방향 변환 일관성 검증")
    fun bidirectionalConversion() {
        // given
        val originalDomain = ErrorCode(
            code = "TEST001",
            message = "Test message",
            description = "Test description",
            category = "TEST"
        )
        
        // when
        val entity = mapper.toEntity(originalDomain)
        val convertedDomain = mapper.toDomain(entity)
        
        // then
        assertThat(convertedDomain).isEqualTo(originalDomain)
    }
}
