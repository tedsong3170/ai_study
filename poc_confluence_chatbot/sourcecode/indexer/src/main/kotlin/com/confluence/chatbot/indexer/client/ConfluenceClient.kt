package com.confluence.chatbot.indexer.client

import com.confluence.chatbot.indexer.config.AppConfig
import com.confluence.chatbot.indexer.model.ConfluencePage
import com.confluence.chatbot.indexer.model.ConfluencePageDetailResponse
import com.confluence.chatbot.indexer.model.ConfluencePageListResponse
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import java.util.Base64
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jsoup.Jsoup

private val logger = KotlinLogging.logger {}

/** Confluence REST API 클라이언트 */
class ConfluenceClient(private val vertx: Vertx, private val config: AppConfig) {
    private val json = Json { ignoreUnknownKeys = true }
    private val webClient: WebClient by lazy {
        val options =
                WebClientOptions()
                        .setDefaultHost(extractHost(config.confluenceBaseUrl))
                        .setDefaultPort(443)
                        .setSsl(true)
                        .setTrustAll(true)
        WebClient.create(vertx, options)
    }

    private val authHeader: String by lazy {
        val credentials = "${config.confluenceUserEmail}:${config.confluenceApiToken}"
        "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())
    }

    /** Space 내 모든 페이지 ID 조회 */
    suspend fun getPageIds(spaceKey: String): List<String> {
        val pageIds = mutableListOf<String>()
        var start = 0
        val limit = 25

        do {
            val response =
                    webClient
                            .get("/wiki/rest/api/content")
                            .addQueryParam("spaceKey", spaceKey)
                            .addQueryParam("type", "page")
                            .addQueryParam("status", "current")
                            .addQueryParam("start", start.toString())
                            .addQueryParam("limit", limit.toString())
                            .putHeader("Authorization", authHeader)
                            .putHeader("Accept", "application/json")
                            .send()
                            .await()

            if (response.statusCode() != 200) {
                logger.error {
                    "Failed to get pages: ${response.statusCode()} - ${response.bodyAsString()}"
                }
                break
            }

            val pageList =
                    json.decodeFromString<ConfluencePageListResponse>(response.bodyAsString())
            pageIds.addAll(pageList.results.map { it.id })

            start += limit

            // 다음 페이지가 없으면 종료
            if (pageList.results.size < limit) break
        } while (true)

        logger.info { "Found ${pageIds.size} pages in space $spaceKey" }
        return pageIds
    }

    /** 페이지 상세 정보 조회 */
    suspend fun getPage(pageId: String): ConfluencePage {
        val response =
                webClient
                        .get("/wiki/rest/api/content/$pageId")
                        .addQueryParam("expand", "body.storage,space,version,metadata.labels")
                        .putHeader("Authorization", authHeader)
                        .putHeader("Accept", "application/json")
                        .send()
                        .await()

        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to get page $pageId: ${response.statusCode()}")
        }

        val detail = json.decodeFromString<ConfluencePageDetailResponse>(response.bodyAsString())
        val htmlContent = detail.body?.storage?.value ?: ""
        val textContent = parseHtml(htmlContent)

        // Labels를 카테고리로 사용 (없으면 Space 이름 사용)
        val labels = detail.metadata?.labels?.results?.map { it.name } ?: emptyList()
        val category =
                if (labels.isNotEmpty()) {
                    labels.joinToString(", ")
                } else {
                    detail.space?.name ?: detail.space?.key ?: "일반"
                }

        return ConfluencePage(
                id = detail.id,
                spaceKey = detail.space?.key ?: config.confluenceSpaceKey,
                title = detail.title,
                content = textContent,
                category = category,
                url = buildPageUrl(detail),
                lastModified = detail.version?.when_
        )
    }

    /** HTML을 텍스트로 변환 */
    private fun parseHtml(html: String): String {
        if (html.isBlank()) return ""

        val doc = Jsoup.parse(html)
        // 불필요한 요소 제거
        doc.select("script, style, nav, footer, header").remove()

        return doc.text()
    }

    private fun buildPageUrl(detail: ConfluencePageDetailResponse): String {
        val baseUrl = config.confluenceBaseUrl.trimEnd('/')
        val webui = detail._links?.webui ?: "/wiki/spaces/${detail.space?.key}/pages/${detail.id}"
        return "$baseUrl$webui"
    }

    private fun extractHost(url: String): String {
        return url.removePrefix("https://").removePrefix("http://").split("/").first()
    }
}
