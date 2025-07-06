package com.example.markdown.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView


object MarkdownRenderer {
    suspend fun render(context: Context, elements: List<MarkdownElement>,
                       loadImage: suspend (String) -> Bitmap?): List<View> {
        return elements.map { element ->
            when (element) {
                is Heading -> {
                    val textView = TextView(context)
                    textView.textSize = (24 - (element.level * 2)).toFloat()
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.text = inlineToSpanned(element.elements)
                    textView
                }

                is Paragraph -> {
                    val textView = TextView(context)
                    textView.text = inlineToSpanned(element.elements)
                    textView
                }
                is Image -> {
                    val imageView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        contentDescription = element.alt
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }

                    val bitmap = loadImage(element.url)
                    bitmap?.let { imageView.setImageBitmap(it) }

                    imageView
                }

                is Table -> {
                    val tableLayout = TableLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        isStretchAllColumns = true
                    }

                    element.rows.forEachIndexed { rowIndex, row ->
                        val tableRow = TableRow(context)
                        row.cells.forEach { cellElements ->
                            val textView = TextView(context).apply {
                                text = inlineToSpanned(cellElements)
                                setPadding(8, 8, 8, 8)
                                if (rowIndex == 0) {
                                    setTypeface(null, Typeface.BOLD)
                                    setBackgroundColor(Color.LTGRAY)
                                }
                            }
                            tableRow.addView(textView)
                        }
                        tableLayout.addView(tableRow)
                    }
                    tableLayout
                }

            }
        }
    }

    private fun inlineToSpanned(inlines: List<MarkdownInlineElement>): Spanned {
        val builder = SpannableStringBuilder()

        inlines.forEach { inline ->
            val start = builder.length
            when (inline) {
                is Text -> builder.append(inline.text)
                is Bold -> {
                    builder.append(inline.text)
                    builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                is Italic -> {
                    builder.append(inline.text)
                    builder.setSpan(StyleSpan(Typeface.ITALIC), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                is Strikethrough -> {
                    builder.append(inline.text)
                    builder.setSpan(StrikethroughSpan(), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        return builder
    }
}
