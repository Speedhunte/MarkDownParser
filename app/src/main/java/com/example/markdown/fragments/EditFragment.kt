package com.example.markdown.fragments

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.markdown.MainViewModel
import com.example.markdown.MarkDownState
import com.example.markdown.R
import kotlinx.coroutines.launch


class EditFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var editText: EditText
    private lateinit var saveButton: Button

    private lateinit var boldButton: Button
    private lateinit var italicButton: Button
    private lateinit var strikeThroughButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editText = view.findViewById(R.id.edit_markdown)
        saveButton = view.findViewById(R.id.button_save)
        italicButton = view.findViewById(R.id.button_italic)
        boldButton = view. findViewById(R.id.button_bold)
        strikeThroughButton= view. findViewById(R.id.button_strike)

        strikeThroughButton.paintFlags = strikeThroughButton.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if(state is MarkDownState.Success){
                    editText.setText(state.markdown)
                    saveButton.isEnabled = state.markdown.isBlank()
                }
            }
        }

        saveButton.setOnClickListener {
            val newText = editText.text.toString()
            viewModel.setMarkdownContent(newText)
            Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show()
        }
        boldButton.setOnClickListener {
            wrapSelection("**", "**")
        }

        italicButton.setOnClickListener {
            wrapSelection("*", "*")
        }

        strikeThroughButton.setOnClickListener {
            wrapSelection("~~", "~~")
        }
    }

    fun wrapSelection(startToken: String, endToken: String) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < 0 || end <= start) return

        val selectedText = editText.text.substring(start, end)
        val newText = "$startToken$selectedText$endToken"

        editText.text.replace(start, end, newText)
        editText.setSelection(start, start + newText.length)
    }
}
