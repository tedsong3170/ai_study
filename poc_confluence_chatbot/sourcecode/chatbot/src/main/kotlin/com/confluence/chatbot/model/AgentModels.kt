package com.confluence.chatbot.model

import kotlinx.serialization.Serializable

/** Agent API 요청/응답 모델 */

/** Agent 질의 요청 */
@Serializable
data class AgentQueryRequest(
        val question: String,
        val top_k: Int = 5,
        val space_key: String? = null,
        val chat_history: List<AgentChatMessage>? = null
)

/** Agent 대화 메시지 */
@Serializable data class AgentChatMessage(val role: String, val content: String)

/** Agent 질의 응답 */
@Serializable
data class AgentQueryResponse(
        val answer: String,
        val sources: List<AgentSource>,
        val processing_time_ms: Int
)

/** Agent 출처 */
@Serializable
data class AgentSource(
        val page_id: String,
        val title: String,
        val url: String,
        val chunk_content: String,
        val relevance_score: Double
)
