package com.confluence.chatbot.model

import kotlinx.serialization.Serializable

/** 채팅 요청 */
@Serializable data class ChatRequest(val question: String, val conversation_id: String? = null)

/** 채팅 응답 */
@Serializable
data class ChatResponse(val conversation_id: String, val answer: String, val sources: List<Source>)

/** 출처 정보 */
@Serializable
data class Source(
        val page_id: String,
        val title: String,
        val url: String,
        val chunk_content: String,
        val relevance_score: Double
)

/** 대화 메시지 (Conversation Memory) */
@Serializable
data class ChatMessage(
        val role: String, // "user" | "assistant"
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
)

/** 대화 이력 */
@Serializable
data class Conversation(
        val id: String,
        val messages: MutableList<ChatMessage> = mutableListOf(),
        val createdAt: Long = System.currentTimeMillis()
)

/** 대화 이력 응답 */
@Serializable
data class HistoryResponse(val conversation_id: String, val messages: List<ChatMessage>)
