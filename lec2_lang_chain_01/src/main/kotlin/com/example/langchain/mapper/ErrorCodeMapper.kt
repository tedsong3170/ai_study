package com.example.langchain.mapper

import com.example.langchain.domain.ErrorCode
import com.example.langchain.entity.ErrorCodeEntity
import org.springframework.stereotype.Component

/**
 * ErrorCode Domain ↔ ErrorCodeEntity 변환 Mapper
 */
@Component
class ErrorCodeMapper {
    
    /**
     * Domain 객체를 Entity로 변환
     */
    fun toEntity(domain: ErrorCode): ErrorCodeEntity {
        return ErrorCodeEntity(
            code = domain.code,
            message = domain.message,
            description = domain.description,
            category = domain.category
        )
    }
    
    /**
     * Entity를 Domain 객체로 변환
     */
    fun toDomain(entity: ErrorCodeEntity): ErrorCode {
        return ErrorCode(
            code = entity.code,
            message = entity.message,
            description = entity.description,
            category = entity.category
        )
    }
    
    /**
     * Entity 리스트를 Domain 리스트로 변환
     */
    fun toDomainList(entities: List<ErrorCodeEntity>): List<ErrorCode> {
        return entities.map { toDomain(it) }
    }
}
