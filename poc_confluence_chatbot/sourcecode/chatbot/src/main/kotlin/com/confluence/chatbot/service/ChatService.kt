package com.confluence.chatbot.service

import com.confluence.chatbot.client.AgentClient
import com.confluence.chatbot.config.AppConfig
import com.confluence.chatbot.model.*
import com.confluence.chatbot.store.ConversationStore
import io.vertx.core.Vertx
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 채팅 서비스
 *
 * - 대화 관리
 * - Agent 호출
 * - Conversation Memory 구현
 */
class ChatService(vertx: Vertx, config: AppConfig) {
    private val agentClient = AgentClient(vertx, config)
    private val conversationStore = ConversationStore(config.maxChatHistoryTurns)

    /**
     * 질문에 대한 답변 생성
     *
     * 1. 대화 이력 조회/생성
     * 2. 이전 대화 이력 가져오기 (Conversation Memory)
     * 3. Agent에 질의 (chat_history 포함)
     * 4. 질문/답변 저장
     * 5. 응답 반환
     */
    suspend fun chat(request: ChatRequest): ChatResponse {
        logger.info { "Processing chat request: ${request.question}" }

        // 1. 대화 조회/생성
        val conversation = conversationStore.getOrCreateConversation(request.conversation_id)

        // 2. 이전 대화 이력 가져오기 (Conversation Memory)
        val chatHistory =
                conversationStore.getRecentHistory(conversation.id).map {
                    AgentChatMessage(role = it.role, content = it.content)
                }

        logger.debug { "Chat history: ${chatHistory.size} messages" }

        // 3. Agent에 질의
        val agentRequest =
                AgentQueryRequest(
                        question = request.question,
                        chat_history = chatHistory.ifEmpty { null }
                )

        val agentResponse = agentClient.query(agentRequest)

        // 4. 질문/답변 저장
        conversationStore.addMessage(conversation.id, "user", request.question)
        conversationStore.addMessage(conversation.id, "assistant", agentResponse.answer)

        // 5. 응답 반환
        return ChatResponse(
                conversation_id = conversation.id,
                answer = agentResponse.answer,
                sources =
                        agentResponse.sources.map { source ->
                            Source(
                                    page_id = source.page_id,
                                    title = source.title,
                                    url = source.url,
                                    chunk_content = source.chunk_content,
                                    relevance_score = source.relevance_score
                            )
                        }
        )
    }

    /** 대화 이력 조회 */
    fun getHistory(conversationId: String): HistoryResponse? {
        val conversation = conversationStore.getConversation(conversationId) ?: return null

        return HistoryResponse(
                conversation_id = conversation.id,
                messages = conversation.messages.toList()
        )
    }

    /** Agent 헬스체크 */
    suspend fun checkAgentHealth(): Boolean {
        return agentClient.healthCheck()
    }
}
