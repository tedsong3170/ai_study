package com.example.langchain.domain

/**
 * 에러 코드 도메인 모델
 * 
 * @property code 에러 코드 (예: "ERR001")
 * @property message 에러 메시지
 * @property description 상세 설명
 * @property category 카테고리 (예: "AUTHENTICATION", "VALIDATION")
 */
data class ErrorCode(
    val code: String,
    val message: String,
    val description: String,
    val category: String
) {
    init {
        require(code.isNotBlank()) { "Error code must not be blank" }
        require(message.isNotBlank()) { "Error message must not be blank" }
        require(description.isNotBlank()) { "Error description must not be blank" }
        require(category.isNotBlank()) { "Error category must not be blank" }
    }
}
