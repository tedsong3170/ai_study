package com.confluence.chatbot.verticle

import com.confluence.chatbot.config.AppConfig
import com.confluence.chatbot.model.ChatRequest
import com.confluence.chatbot.service.ChatService
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json as KotlinJson
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** HTTP 서버 Verticle */
class HttpServerVerticle(private val appConfig: AppConfig) : CoroutineVerticle() {

    private val json = KotlinJson { ignoreUnknownKeys = true }
    private lateinit var chatService: ChatService

    override suspend fun start() {
        chatService = ChatService(vertx, appConfig)

        val router = createRouter()

        vertx.createHttpServer().requestHandler(router).listen(appConfig.chatbotPort)

        logger.info { "HTTP Server started on port ${appConfig.chatbotPort}" }
    }

    private fun createRouter(): Router {
        val router = Router.router(vertx)

        // CORS
        router.route()
                .handler(
                        CorsHandler.create()
                                .addOrigin("*")
                                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                                .allowedHeader("Content-Type")
                )

        // Body 파싱
        router.route().handler(BodyHandler.create())

        // 라우트 등록
        router.post("/api/chat").handler { ctx -> handleChat(ctx) }
        router.get("/api/chat/history").handler { ctx -> handleHistory(ctx) }
        router.get("/health").handler { ctx -> handleHealth(ctx) }

        return router
    }

    /** POST /api/chat - 채팅 API */
    private fun handleChat(ctx: RoutingContext) {
        launch(vertx.dispatcher()) {
            try {
                val body = ctx.body().asString()
                val request = json.decodeFromString<ChatRequest>(body)

                val response = chatService.chat(request)

                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(json.encodeToString(response))
            } catch (e: Exception) {
                logger.error(e) { "Chat error" }
                ctx.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end("""{"error": "${e.message}"}""")
            }
        }
    }

    /** GET /api/chat/history - 대화 이력 조회 */
    private fun handleHistory(ctx: RoutingContext) {
        val conversationId = ctx.queryParam("conversation_id").firstOrNull()

        if (conversationId.isNullOrBlank()) {
            ctx.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end("""{"error": "conversation_id is required"}""")
            return
        }

        val history = chatService.getHistory(conversationId)

        if (history == null) {
            ctx.response()
                    .setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end("""{"error": "Conversation not found"}""")
            return
        }

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(json.encodeToString(history))
    }

    /** GET /health - 헬스체크 */
    private fun handleHealth(ctx: RoutingContext) {
        launch(vertx.dispatcher()) {
            val agentHealthy = chatService.checkAgentHealth()

            val status = if (agentHealthy) "healthy" else "degraded"

            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end("""{"status": "$status", "agent": "$agentHealthy"}""")
        }
    }
}
