package com.example.langchain.entity

import jakarta.persistence.*

/**
 * 에러 코드 JPA Entity (Persistence Layer)
 */
@Entity
@Table(name = "error_codes")
class ErrorCodeEntity(
    @Id
    @Column(length = 20, nullable = false)
    val code: String = "",
    
    @Column(nullable = false, length = 500)
    val message: String = "",
    
    @Column(nullable = false, length = 2000)
    val description: String = "",
    
    @Column(nullable = false, length = 50)
    val category: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ErrorCodeEntity) return false
        return code == other.code
    }
    
    override fun hashCode(): Int = code.hashCode()
    
    override fun toString(): String {
        return "ErrorCodeEntity(code='$code', message='$message', category='$category')"
    }
}

