package com.confluence.chatbot.indexer.model

import kotlinx.serialization.Serializable

/** Agent API 요청/응답 모델 */

/** 임베딩 요청 */
@Serializable
data class EmbedRequest(
        val page_id: String,
        val space_key: String,
        val title: String,
        val content: String,
        val category: String = "",
        val page_url: String
)

/** 임베딩 응답 */
@Serializable
data class EmbedResponse(
        val status: String,
        val page_id: String,
        val chunks_created: Int,
        val message: String
)

/** 색인 결과 */
data class IndexingResult(
        val jobId: String,
        val totalDocuments: Int,
        val processedDocuments: Int,
        val failedDocuments: Int,
        val startedAt: String,
        val completedAt: String? = null
)
