package com.confluence.chatbot.config

/** 애플리케이션 설정 */
data class AppConfig(
        val agentBaseUrl: String,
        val chatbotPort: Int = 8080,
        val maxChatHistoryTurns: Int = 5
) {
    companion object {
        fun load(): AppConfig {
            return AppConfig(
                    agentBaseUrl = getEnv("AGENT_BASE_URL", "http://localhost:8000"),
                    chatbotPort = getEnv("CHATBOT_PORT", "8080").toInt(),
                    maxChatHistoryTurns = getEnv("MAX_CHAT_HISTORY_TURNS", "5").toInt()
            )
        }

        private fun getEnv(name: String, default: String): String {
            return System.getenv(name) ?: default
        }
    }
}
