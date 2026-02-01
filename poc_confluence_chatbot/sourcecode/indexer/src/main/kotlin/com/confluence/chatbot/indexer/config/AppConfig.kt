package com.confluence.chatbot.indexer.config

import java.io.File
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** 애플리케이션 설정 */
data class AppConfig(
        val confluenceBaseUrl: String,
        val confluenceApiToken: String,
        val confluenceUserEmail: String,
        val confluenceSpaceKey: String,
        val agentBaseUrl: String,
        val indexerPort: Int = 8081
) {
    companion object {
        private val envVars = mutableMapOf<String, String>()

        init {
            loadEnvFile()
        }

        /** .env 파일 로드 */
        private fun loadEnvFile() {
            val envFile = File(".env")
            if (envFile.exists()) {
                logger.info { "✅ .env 파일 로드: ${envFile.absolutePath}" }
                envFile.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        val (key, value) = trimmed.split("=", limit = 2)
                        val cleanKey = key.replace("export ", "").trim()
                        val cleanValue = value.trim().removeSurrounding("\"").removeSurrounding("'")
                        envVars[cleanKey] = cleanValue
                    }
                }
            } else {
                logger.warn { "⚠️ .env 파일 없음: ${envFile.absolutePath}" }
            }
        }

        /** 환경 변수에서 설정 로드 */
        fun load(): AppConfig {
            return AppConfig(
                    confluenceBaseUrl = requireEnv("CONFLUENCE_BASE_URL"),
                    confluenceApiToken = requireEnv("CONFLUENCE_API_TOKEN"),
                    confluenceUserEmail = requireEnv("CONFLUENCE_USER_EMAIL"),
                    confluenceSpaceKey = requireEnv("CONFLUENCE_SPACE_KEY"),
                    agentBaseUrl = getEnv("AGENT_BASE_URL", "http://localhost:8000"),
                    indexerPort = getEnv("INDEXER_PORT", "8081").toInt()
            )
        }

        private fun requireEnv(name: String): String {
            return envVars[name]
                    ?: System.getenv(name)
                            ?: throw IllegalStateException("Environment variable $name is required")
        }

        private fun getEnv(name: String, default: String): String {
            return envVars[name] ?: System.getenv(name) ?: default
        }
    }
}
