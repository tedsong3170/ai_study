package com.example.langchain.config

import com.example.langchain.domain.ErrorCode
import com.example.langchain.mapper.ErrorCodeMapper
import com.example.langchain.repository.ErrorCodeJpaRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * 애플리케이션 시작 시 초기 에러 코드 데이터를 DB에 로딩
 */
@Component
class DataInitializer(
    private val jpaRepository: ErrorCodeJpaRepository,
    private val mapper: ErrorCodeMapper
) : CommandLineRunner {
    
    override fun run(vararg args: String?) {
        val initialData = listOf(
            ErrorCode(
                code = "ERR001",
                message = "Invalid credentials",
                description = "The provided username or password is incorrect. Please verify your credentials and try again.",
                category = "AUTHENTICATION"
            ),
            ErrorCode(
                code = "ERR002",
                message = "Account locked",
                description = "Your account has been locked due to multiple failed login attempts. Please contact support or reset your password.",
                category = "AUTHENTICATION"
            ),
            ErrorCode(
                code = "ERR003",
                message = "Session expired",
                description = "Your session has expired due to inactivity. Please log in again to continue.",
                category = "AUTHENTICATION"
            ),
            ErrorCode(
                code = "ERR101",
                message = "Missing required field",
                description = "A required field was not provided in the request. Please check your input and ensure all required fields are included.",
                category = "VALIDATION"
            ),
            ErrorCode(
                code = "ERR102",
                message = "Invalid email format",
                description = "The email address provided is not in a valid format. Please enter a valid email address.",
                category = "VALIDATION"
            ),
            ErrorCode(
                code = "ERR103",
                message = "Password too weak",
                description = "The password does not meet the security requirements. It must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters.",
                category = "VALIDATION"
            ),
            ErrorCode(
                code = "ERR201",
                message = "Resource not found",
                description = "The requested resource could not be found. Please check the resource ID and try again.",
                category = "RESOURCE"
            ),
            ErrorCode(
                code = "ERR202",
                message = "Duplicate resource",
                description = "A resource with the same identifier already exists. Please use a different identifier or update the existing resource.",
                category = "RESOURCE"
            ),
            ErrorCode(
                code = "ERR301",
                message = "Permission denied",
                description = "You do not have sufficient permissions to perform this action. Please contact your administrator.",
                category = "AUTHORIZATION"
            ),
            ErrorCode(
                code = "ERR302",
                message = "Role not assigned",
                description = "The required role is not assigned to your account. Please request the necessary role from your administrator.",
                category = "AUTHORIZATION"
            )
        )
        
        // Domain 객체를 Entity로 변환하여 DB에 저장
        val entities = initialData.map { mapper.toEntity(it) }
        jpaRepository.saveAll(entities)
        
        println("✅ Initialized ${entities.size} error codes in the database")
    }
}
