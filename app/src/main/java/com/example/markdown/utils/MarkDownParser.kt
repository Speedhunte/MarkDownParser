package com.example.markdown.utils

import android.util.Log


sealed interface MarkdownElement

data class Paragraph(val elements: List<MarkdownInlineElement>) : MarkdownElement
data class Heading(val level: Int, val elements: List<MarkdownInlineElement>) : MarkdownElement
data class Image(val url: String,  val alt: String): MarkdownElement
data class Table(val rows: List<TableRow>) : MarkdownElement
data class TableRow(val cells: List<List<MarkdownInlineElement>>)


sealed interface MarkdownInlineElement
    data class Text(val text: String) : MarkdownInlineElement
    data class Bold(val text: String) : MarkdownInlineElement
    data class Italic(val text: String) : MarkdownInlineElement
    data class Strikethrough(val text: String) : MarkdownInlineElement

object MarkdownParser {
    fun parse(input: String): List<MarkdownElement> {
        val lines = input.lines()
        val elements = mutableListOf<MarkdownElement>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.isEmpty()) {
                i++
                continue
            }
            val headingMatch = Regex("^(#{1,6})\\s+(.*)").find(line)
            if (headingMatch != null) {
                val level = headingMatch.groupValues[1].length
                val content = headingMatch.groupValues[2]
                elements.add(Heading(level, parseInline(content)))
                i++
                continue
            }

            val imageMatch = Regex("""!\[(.*?)]\((.*?)\)""").find(line)
            if (imageMatch != null && line.startsWith("![")) {
                elements.add(Image(imageMatch.groupValues[2], imageMatch.groupValues[1]))
                i++
                continue
            }

            if (line.startsWith("|") && i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                if (Regex("""^\|?(\s*:?-+:?\s*\|)+\s*:?-+:?\s*\|?$""").matches(nextLine)) {
                    val (table, newIndex) = parseTable(lines, i)
                    elements.add(table)
                    i = newIndex + 1
                    continue
                }
            }
            elements.add(Paragraph(parseInline(line)))
            i++
        }

        return elements
    }



    private fun parseInline(text: String): List<MarkdownInlineElement> {
        val result = mutableListOf<MarkdownInlineElement>()
        var remaining = text

        val patterns = listOf(
            Regex("""\*\*(.+?)\*\*""") to { m: MatchResult -> Bold(m.groupValues[1]) },
            Regex("""\*(.+?)\*""") to { m: MatchResult -> Italic(m.groupValues[1]) },
            Regex("""~~(.+?)~~""") to { m: MatchResult -> Strikethrough(m.groupValues[1]) }
        )

        while (remaining.isNotEmpty()) {
            var firstMatch: MatchResult? = null
            var constructor: ((MatchResult) -> MarkdownInlineElement)? = null

            for ((regex, builder) in patterns) {
                val match = regex.find(remaining)
                if (match != null) {
                    if (firstMatch == null || match.range.first < firstMatch.range.first) {
                        firstMatch = match
                        constructor = builder
                    }
                }
            }

            if (firstMatch != null && constructor != null) {
                if (firstMatch.range.first > 0) {
                    val before = remaining.substring(0, firstMatch.range.first)
                    result.add(Text(before))
                }

                result.add(constructor(firstMatch))

                remaining = remaining.substring(firstMatch.range.last + 1)
            } else {
                result.add(Text(remaining))
                break
            }
        }

        return result
    }

    private fun parseTable(lines: List<String>, startIndex: Int): Pair<Table, Int> {
        val rows = mutableListOf<TableRow>()
        var index = startIndex

        while (index < lines.size) {
            val line = lines[index].trim()
            if (line.isEmpty()) break
            if (index == startIndex + 1) {
                val isSeparator = Regex("""^\|?(\s*:?-+:?\s*\|)+\s*:?-+:?\s*\|?$""").matches(line)
                if (!isSeparator) break
                index++
                continue
            }

            val rawCells = line.removePrefix("|").removeSuffix("|").split("|")
            val cells = rawCells.map { parseInline(it.trim()) }
            rows.add(TableRow(cells))
            index++
        }

        return Table(rows) to (index - 1)
    }
}
