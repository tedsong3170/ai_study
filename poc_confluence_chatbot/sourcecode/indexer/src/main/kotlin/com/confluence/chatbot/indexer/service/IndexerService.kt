package com.confluence.chatbot.indexer.service

import com.confluence.chatbot.indexer.client.AgentClient
import com.confluence.chatbot.indexer.client.ConfluenceClient
import com.confluence.chatbot.indexer.config.AppConfig
import com.confluence.chatbot.indexer.model.EmbedRequest
import com.confluence.chatbot.indexer.model.IndexingResult
import io.vertx.core.Vertx
import java.time.Instant
import java.util.UUID
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** 문서 색인 서비스 */
class IndexerService(private val vertx: Vertx, private val config: AppConfig) {
    private val confluenceClient = ConfluenceClient(vertx, config)
    private val agentClient = AgentClient(vertx, config)

    /** 색인 작업 실행 */
    suspend fun runIndexing(): IndexingResult {
        val jobId = UUID.randomUUID().toString()
        val startedAt = Instant.now().toString()

        logger.info { "Starting indexing job: $jobId" }

        // Agent 헬스체크
        if (!agentClient.healthCheck()) {
            logger.error { "Agent service is not available" }
            return IndexingResult(
                    jobId = jobId,
                    totalDocuments = 0,
                    processedDocuments = 0,
                    failedDocuments = 0,
                    startedAt = startedAt,
                    completedAt = Instant.now().toString()
            )
        }

        // 페이지 목록 조회
        val pageIds = confluenceClient.getPageIds(config.confluenceSpaceKey)
        val totalDocuments = pageIds.size

        var processedDocuments = 0
        var failedDocuments = 0

        // 각 페이지 색인
        for (pageId in pageIds) {
            try {
                val page = confluenceClient.getPage(pageId)

                // 내용이 없으면 스킵
                if (page.content.isBlank()) {
                    logger.debug { "Skipping empty page: ${page.title}" }
                    continue
                }

                val request =
                        EmbedRequest(
                                page_id = page.id,
                                space_key = page.spaceKey,
                                title = page.title,
                                content = page.content,
                                category = page.category,
                                page_url = page.url
                        )

                val response = agentClient.embed(request)
                logger.info { "Indexed: ${page.title} (${response.chunks_created} chunks)" }

                processedDocuments++
            } catch (e: Exception) {
                logger.error(e) { "Failed to index page: $pageId" }
                failedDocuments++
            }
        }

        return IndexingResult(
                jobId = jobId,
                totalDocuments = totalDocuments,
                processedDocuments = processedDocuments,
                failedDocuments = failedDocuments,
                startedAt = startedAt,
                completedAt = Instant.now().toString()
        )
    }
}
