package com.example.markdown.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.markdown.MainViewModel
import com.example.markdown.MarkDownState
import com.example.markdown.R
import com.example.markdown.utils.MarkdownParser
import com.example.markdown.utils.MarkdownRenderer
import kotlinx.coroutines.launch


class ViewFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_view, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val container = (view as ScrollView).getChildAt(0) as LinearLayout

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                container.removeAllViews()
                if (state is MarkDownState.Success) {
                    val parsed = MarkdownParser.parse(state.markdown)
                    val views = MarkdownRenderer.render(
                        requireContext(),
                        parsed,
                        viewModel::loadImage)
                    views.forEach { container.addView(it) }
                }
            }
        }
    }
}
