package com.confluence.chatbot.store

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConversationStoreTest {

    private lateinit var store: ConversationStore

    @BeforeEach
    fun setup() {
        store = ConversationStore(maxHistoryTurns = 3)
    }

    @Test
    fun `createConversation should return new conversation with unique id`() {
        val conv1 = store.createConversation()
        val conv2 = store.createConversation()

        assertNotNull(conv1.id)
        assertNotNull(conv2.id)
        assertNotEquals(conv1.id, conv2.id)
    }

    @Test
    fun `getOrCreateConversation should return existing conversation`() {
        val conv = store.createConversation()
        val retrieved = store.getOrCreateConversation(conv.id)

        assertEquals(conv.id, retrieved.id)
    }

    @Test
    fun `getOrCreateConversation should create new when id is null`() {
        val conv = store.getOrCreateConversation(null)

        assertNotNull(conv.id)
    }

    @Test
    fun `addMessage should add message to conversation`() {
        val conv = store.createConversation()

        store.addMessage(conv.id, "user", "Hello")
        store.addMessage(conv.id, "assistant", "Hi there!")

        val retrieved = store.getConversation(conv.id)

        assertEquals(2, retrieved?.messages?.size)
        assertEquals("user", retrieved?.messages?.get(0)?.role)
        assertEquals("Hello", retrieved?.messages?.get(0)?.content)
    }

    @Test
    fun `getRecentHistory should return limited messages for Conversation Memory`() {
        val conv = store.createConversation()

        // 5턴 추가 (10개 메시지)
        repeat(5) { i ->
            store.addMessage(conv.id, "user", "Question $i")
            store.addMessage(conv.id, "assistant", "Answer $i")
        }

        // maxHistoryTurns=3 이므로 최근 6개 메시지만 반환
        val history = store.getRecentHistory(conv.id)

        assertEquals(6, history.size)
        assertEquals("Question 2", history[0].content) // 가장 오래된 것이 첫 번째
    }

    @Test
    fun `deleteConversation should remove conversation`() {
        val conv = store.createConversation()

        assertTrue(store.deleteConversation(conv.id))
        assertNull(store.getConversation(conv.id))
    }
}
