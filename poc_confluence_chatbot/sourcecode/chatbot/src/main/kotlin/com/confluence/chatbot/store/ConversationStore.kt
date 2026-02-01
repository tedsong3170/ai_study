package com.confluence.chatbot.store

import com.confluence.chatbot.model.ChatMessage
import com.confluence.chatbot.model.Conversation
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 대화 이력 저장소 (인메모리)
 *
 * POC 단계에서는 인메모리 저장, 추후 DB 연동 가능
 */
class ConversationStore(private val maxHistoryTurns: Int = 5) {
    private val conversations = ConcurrentHashMap<String, Conversation>()

    /** 새 대화 생성 */
    fun createConversation(): Conversation {
        val id = UUID.randomUUID().toString()
        val conversation = Conversation(id = id)
        conversations[id] = conversation
        logger.debug { "Created conversation: $id" }
        return conversation
    }

    /** 대화 조회 (없으면 생성) */
    fun getOrCreateConversation(conversationId: String?): Conversation {
        if (conversationId.isNullOrBlank()) {
            return createConversation()
        }

        return conversations.getOrPut(conversationId) { Conversation(id = conversationId) }
    }

    /** 대화에 메시지 추가 */
    fun addMessage(conversationId: String, role: String, content: String) {
        val conversation = conversations[conversationId] ?: return

        conversation.messages.add(ChatMessage(role = role, content = content))

        logger.debug { "Added message to $conversationId: $role" }
    }

    /**
     * 최근 대화 이력 조회 (Conversation Memory용)
     *
     * @return 최근 N턴의 메시지 (user/assistant 쌍)
     */
    fun getRecentHistory(conversationId: String): List<ChatMessage> {
        val conversation = conversations[conversationId] ?: return emptyList()

        // 최근 N턴 * 2 (user + assistant)
        val maxMessages = maxHistoryTurns * 2
        val messages = conversation.messages

        return if (messages.size <= maxMessages) {
            messages.toList()
        } else {
            messages.takeLast(maxMessages)
        }
    }

    /** 대화 이력 전체 조회 */
    fun getConversation(conversationId: String): Conversation? {
        return conversations[conversationId]
    }

    /** 대화 삭제 */
    fun deleteConversation(conversationId: String): Boolean {
        return conversations.remove(conversationId) != null
    }
}
