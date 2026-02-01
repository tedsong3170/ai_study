package com.confluence.chatbot

import com.confluence.chatbot.config.AppConfig
import com.confluence.chatbot.verticle.HttpServerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Chatbot 애플리케이션 진입점 */
suspend fun main() {
    logger.info { "Starting Confluence Chatbot..." }

    val vertx = Vertx.vertx()

    try {
        val config = AppConfig.load()
        logger.info { "Configuration loaded: port=${config.chatbotPort}" }

        // HTTP 서버 Verticle 배포
        vertx.deployVerticle(HttpServerVerticle(config)).await()

        logger.info { "Chatbot started on port ${config.chatbotPort}" }
    } catch (e: Exception) {
        logger.error(e) { "Failed to start Chatbot" }
        vertx.close()
    }
}
