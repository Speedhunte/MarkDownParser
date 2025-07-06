package com.example.markdown.models

data class LoadScreenState(
    val editMode: Boolean,
    val url: String
)
sealed class LoadScreenEvent{
    data class OnUrlUpdated(val newUrl: String): LoadScreenEvent()
    data class OnEditModeToggled(val newSet: Boolean): LoadScreenEvent()
}