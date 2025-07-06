package com.example.markdown.models

sealed class MarkDownState{
    object Dummy: MarkDownState()
    object Loading: MarkDownState()
    data class  Error( val error: String): MarkDownState()
    data class Success(val markdown: String): MarkDownState()
}