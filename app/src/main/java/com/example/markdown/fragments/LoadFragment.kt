package com.example.markdown.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.markdown.LoadScreenEvent
import com.example.markdown.MainViewModel
import com.example.markdown.MarkDownState
import com.example.markdown.R
import kotlinx.coroutines.launch

class LoadFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var urlEditText: EditText
    private lateinit var loadButton: Button
    private lateinit var pickFileButton: Button
    private lateinit var modeSwitch: Switch
    private lateinit var nextButton: Button
    private lateinit var progressBar: ProgressBar


    private val filePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            uri?.let { viewModel.loadMarkdownFromFile(requireContext(), it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_load, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        urlEditText = view.findViewById(R.id.edit_url)
        loadButton = view.findViewById(R.id.button_load_url)
        pickFileButton= view.findViewById(R.id.button_pick_file)
        modeSwitch = view.findViewById(R.id.switch_mode)
        nextButton = view.findViewById(R.id.button_next)
        progressBar = view.findViewById(R.id.progress_shimmer)


        loadButton.setOnClickListener {
            val url = urlEditText.text.toString()
            if (url.isNotBlank()) {
                viewModel.loadMarkdownFromUrl(url)
            } else {
                Toast.makeText(requireContext(), "Введите URL", Toast.LENGTH_SHORT).show()
            }
        }

        pickFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "text/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePicker.launch(intent)
        }

        urlEditText.setOnClickListener {
            viewModel.onLoadScreenEvent(LoadScreenEvent.OnUrlUpdated(it.toString()))
        }


        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onLoadScreenEvent(LoadScreenEvent.OnEditModeToggled(isChecked))
        }

        nextButton.setOnClickListener {
            val navController = findNavController()
            if (viewModel.loadScreenState.value.editMode) {
                navController.navigate(R.id.action_loadFragment_to_editFragment)
            } else {
                navController.navigate(R.id.action_loadFragment_to_viewFragment)
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                renderState(state)
            }
        }


    }

    private fun renderState(state: MarkDownState) {
        when (state) {
            is MarkDownState.Loading -> {
                progressBar.visibility = View.VISIBLE
                nextButton.isEnabled = false
            }
            is MarkDownState.Success -> {
                progressBar.visibility = View.GONE
                nextButton.isEnabled = true
            }
            is MarkDownState.Error -> {
                progressBar.visibility = View.GONE
                nextButton.isEnabled = false
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
            }
            is MarkDownState.Dummy -> {
                progressBar.visibility = View.GONE
                nextButton.isEnabled = false
            }
        }
    }


}