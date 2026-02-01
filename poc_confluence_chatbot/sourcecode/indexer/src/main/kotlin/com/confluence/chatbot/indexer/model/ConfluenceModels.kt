package com.confluence.chatbot.indexer.model

import kotlinx.serialization.Serializable

/** Confluence 페이지 모델 */
@Serializable
data class ConfluencePage(
        val id: String,
        val spaceKey: String,
        val title: String,
        val content: String, // HTML이 제거된 텍스트
        val category: String = "", // Labels 또는 Space명
        val url: String,
        val lastModified: String? = null
)

/** Confluence API 응답 - 페이지 목록 */
@Serializable
data class ConfluencePageListResponse(
        val results: List<ConfluencePageSummary>,
        val size: Int,
        val limit: Int,
        val start: Int,
        val _links: ConfluenceLinks? = null
)

@Serializable
data class ConfluencePageSummary(
        val id: String,
        val title: String,
        val status: String,
        val _links: ConfluenceLinks? = null
)

@Serializable
data class ConfluenceLinks(
        val webui: String? = null,
        val next: String? = null,
        val base: String? = null
)

/** Confluence API 응답 - 페이지 상세 */
@Serializable
data class ConfluencePageDetailResponse(
        val id: String,
        val title: String,
        val space: ConfluenceSpace? = null,
        val body: ConfluenceBody? = null,
        val version: ConfluenceVersion? = null,
        val metadata: ConfluenceMetadata? = null,
        val _links: ConfluenceLinks? = null
)

@Serializable data class ConfluenceSpace(val key: String, val name: String? = null)

@Serializable
data class ConfluenceBody(
        val storage: ConfluenceStorage? = null,
        val view: ConfluenceStorage? = null
)

@Serializable data class ConfluenceStorage(val value: String, val representation: String? = null)

@Serializable data class ConfluenceVersion(val number: Int, val when_: String? = null)

/** Labels 메타데이터 */
@Serializable data class ConfluenceMetadata(val labels: ConfluenceLabels? = null)

@Serializable data class ConfluenceLabels(val results: List<ConfluenceLabel> = emptyList())

@Serializable data class ConfluenceLabel(val name: String, val prefix: String? = null)
