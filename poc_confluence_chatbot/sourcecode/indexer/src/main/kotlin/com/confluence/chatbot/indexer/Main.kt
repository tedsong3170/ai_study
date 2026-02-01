package com.confluence.chatbot.indexer

import com.confluence.chatbot.indexer.config.AppConfig
import com.confluence.chatbot.indexer.service.IndexerService
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Indexer 애플리케이션 진입점
 */
fun main(args: Array<String>) = runBlocking {
    logger.info { "Starting Confluence Indexer..." }
    
    val vertx = Vertx.vertx()
    
    try {
        val config = AppConfig.load()
        logger.info { "Configuration loaded: space=${config.confluenceSpaceKey}" }
        
        val indexerService = IndexerService(vertx, config)
        
        // 색인 실행
        val result = indexerService.runIndexing()
        
        logger.info { "Indexing completed: ${result.processedDocuments}/${result.totalDocuments} documents processed" }
        
        if (result.failedDocuments > 0) {
            logger.warn { "Failed documents: ${result.failedDocuments}" }
        }
        
    } catch (e: Exception) {
        logger.error(e) { "Indexing failed" }
    } finally {
        vertx.close()
    }
}
