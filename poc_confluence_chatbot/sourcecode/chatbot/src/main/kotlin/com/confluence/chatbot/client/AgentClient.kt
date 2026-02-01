package com.confluence.chatbot.client

import com.confluence.chatbot.config.AppConfig
import com.confluence.chatbot.model.AgentQueryRequest
import com.confluence.chatbot.model.AgentQueryResponse
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Agent 서비스 API 클라이언트 */
class AgentClient(private val vertx: Vertx, private val config: AppConfig) {
    private val json = Json { ignoreUnknownKeys = true }
    private val webClient: WebClient by lazy {
        val (host, port) = parseUrl(config.agentBaseUrl)
        val options = WebClientOptions().setDefaultHost(host).setDefaultPort(port)
        WebClient.create(vertx, options)
    }

    /** RAG 질의 요청 */
    suspend fun query(request: AgentQueryRequest): AgentQueryResponse {
        logger.debug { "Querying Agent: ${request.question}" }

        val response =
                webClient
                        .post("/query")
                        .putHeader("Content-Type", "application/json")
                        .sendBuffer(
                                io.vertx.core.buffer.Buffer.buffer(json.encodeToString(request))
                        )
                        .await()

        if (response.statusCode() != 200) {
            throw RuntimeException(
                    "Agent query failed: ${response.statusCode()} - ${response.bodyAsString()}"
            )
        }

        return json.decodeFromString<AgentQueryResponse>(response.bodyAsString())
    }

    /** 헬스체크 */
    suspend fun healthCheck(): Boolean {
        return try {
            val response = webClient.get("/health").send().await()
            response.statusCode() == 200
        } catch (e: Exception) {
            logger.error(e) { "Agent health check failed" }
            false
        }
    }

    private fun parseUrl(url: String): Pair<String, Int> {
        val withoutProtocol = url.removePrefix("http://").removePrefix("https://")
        val parts = withoutProtocol.split(":")
        val host = parts[0].split("/").first()
        val port = if (parts.size > 1) parts[1].split("/").first().toInt() else 80
        return Pair(host, port)
    }
}
