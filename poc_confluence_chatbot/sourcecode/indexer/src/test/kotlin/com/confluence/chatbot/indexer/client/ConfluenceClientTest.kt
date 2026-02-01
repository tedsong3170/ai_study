package com.confluence.chatbot.indexer.client

import io.mockk.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfluenceClientTest {

    @Test
    fun `parseHtml should extract text from simple HTML`() {
        // Given
        val html = "<p>Hello World</p>"

        // When
        val result = Jsoup.parse(html).text()

        // Then
        assertEquals("Hello World", result)
    }

    @Test
    fun `parseHtml should remove script and style tags`() {
        // Given
        val html =
                """
            <html>
                <head><style>.test { color: red; }</style></head>
                <body>
                    <script>alert('hello');</script>
                    <p>Real content</p>
                </body>
            </html>
        """.trimIndent()

        // When
        val doc = Jsoup.parse(html)
        doc.select("script, style").remove()
        val result = doc.text()

        // Then
        assertEquals("Real content", result)
    }

    @Test
    fun `parseHtml should handle empty HTML`() {
        // Given
        val html = ""

        // When
        val result = Jsoup.parse(html).text()

        // Then
        assertEquals("", result)
    }

    @Test
    fun `parseHtml should extract text from nested elements`() {
        // Given
        val html =
                """
            <div>
                <h1>Title</h1>
                <p>Paragraph 1</p>
                <ul>
                    <li>Item 1</li>
                    <li>Item 2</li>
                </ul>
            </div>
        """.trimIndent()

        // When
        val result = Jsoup.parse(html).text()

        // Then
        assertTrue(result.contains("Title"))
        assertTrue(result.contains("Paragraph 1"))
        assertTrue(result.contains("Item 1"))
        assertTrue(result.contains("Item 2"))
    }

    @Test
    fun `parseHtml should handle Confluence storage format`() {
        // Given - Confluence 저장 형식 예시
        val html =
                """
            <ac:structured-macro ac:name="toc">
                <ac:parameter ac:name="maxLevel">3</ac:parameter>
            </ac:structured-macro>
            <h1>휴가 정책</h1>
            <p>연차휴가는 15일입니다.</p>
            <ac:structured-macro ac:name="info">
                <ac:rich-text-body>
                    <p>중요: 3일 전 신청 필요</p>
                </ac:rich-text-body>
            </ac:structured-macro>
        """.trimIndent()

        // When
        val result = Jsoup.parse(html).text()

        // Then
        assertTrue(result.contains("휴가 정책"))
        assertTrue(result.contains("연차휴가는 15일"))
        assertTrue(result.contains("중요"))
    }
}
