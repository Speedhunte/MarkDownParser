package com.example.markdown

import com.example.markdown.utils.Bold
import com.example.markdown.utils.Heading
import com.example.markdown.utils.Image
import com.example.markdown.utils.MarkdownParser
import com.example.markdown.utils.Paragraph
import com.example.markdown.utils.Strikethrough
import com.example.markdown.utils.Table
import com.example.markdown.utils.Text
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class MarkdownParserTest {

    @Test
    fun `parses plain paragraph`() {
        val input = "This is a paragraph."
        val result = MarkdownParser.parse(input)

        assertEquals(1, result.size)
        val paragraph = result[0] as Paragraph
        assertEquals(1, paragraph.elements.size)
        assertEquals(Text("This is a paragraph."), paragraph.elements[0])
    }

    @Test
    fun `parses bold text`() {
        val input = "**bold**"
        val paragraph = MarkdownParser.parse(input)[0] as Paragraph

        assertEquals(1, paragraph.elements.size)
        assertEquals(Bold("bold"), paragraph.elements[0])
    }

    @Test
    fun `parses heading`() {
        val input = "# Heading Text"
        val heading = MarkdownParser.parse(input)[0] as Heading

        assertEquals(1, heading.elements.size)
        assertEquals(1, heading.level)
        assertEquals(Text("Heading Text"), heading.elements[0])
    }

    @Test
    fun `parses strikethrough`() {
        val input = "~~strike~~"
        val paragraph = MarkdownParser.parse(input)[0] as Paragraph

        assertEquals(Strikethrough("strike"), paragraph.elements[0])
    }

    @Test
    fun `parses image`() {
        val input = "![alt](https://example.com/image.png)"
        val image = MarkdownParser.parse(input)[0] as Image

        assertEquals("https://example.com/image.png", image.url)
        assertEquals("alt", image.alt)
    }

    @Test
    fun `parses table`() {
        val input = """
            |Name|Score|
            |:---|----:|
            |John|  96 |
            |Anna|  88 |
        """.trimIndent()

        val result = MarkdownParser.parse(input)
        assertTrue(result[0] is Table)
        val table = result[0] as Table
        assertEquals(3, table.rows.size)

        val firstCell = table.rows[0].cells[0][0] as Text
        assertEquals("Name", firstCell.text)
    }
}
