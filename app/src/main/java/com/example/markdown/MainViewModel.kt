package com.example.markdown
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


sealed class MarkDownState{
    object Dummy: MarkDownState()
    object Loading: MarkDownState()
    data class  Error( val error: String): MarkDownState()
    data class Success(val markdown: String): MarkDownState()
}

data class LoadScreenState(
    val editMode: Boolean,
    val url: String
)
sealed class LoadScreenEvent{
    data class OnUrlUpdated(val newUrl: String): LoadScreenEvent()
    data class OnEditModeToggled(val newSet: Boolean): LoadScreenEvent()

}
class MainViewModel: ViewModel() {

    private val _state = MutableStateFlow<MarkDownState>(MarkDownState.Dummy)
    val state: StateFlow<MarkDownState> = _state

    private val _loadScreenState = MutableStateFlow(LoadScreenState(false, ""))
    val loadScreenState: StateFlow<LoadScreenState> = _loadScreenState

    private val imageCache = mutableMapOf<String, Bitmap>()


    fun onLoadScreenEvent(event: LoadScreenEvent){
        when(event){
            is LoadScreenEvent.OnEditModeToggled ->
            _loadScreenState.update { it.copy(editMode = event.newSet) }
            is LoadScreenEvent.OnUrlUpdated ->
                _loadScreenState.update { it.copy(url = event.newUrl) }
        }
    }

    suspend fun loadImage(url: String): Bitmap? {
        imageCache[url]?.let { return it }
        return try {
            val bitmap = withContext(Dispatchers.IO) {
                val stream = URL(url).openStream()
                BitmapFactory.decodeStream(stream)
            }
            imageCache[url] = bitmap
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadMarkdownFromUrl(url: String) {
        viewModelScope.launch {
            try {
                _state.value = MarkDownState.Loading
                val text = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 20000
                    connection.readTimeout = 20000

                    connection.inputStream.bufferedReader().use { it.readText() }
                }
                _state.value= MarkDownState.Success(text)
            } catch (e: Exception) {
                _state.value = MarkDownState.Error(e.toString())
            }
        }
    }

    fun loadMarkdownFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }
                _state.value = MarkDownState.Success(text.toString())
            } catch (e: Exception) {
                _state.value= MarkDownState.Error("Ошибка при чтении файла: ${e.localizedMessage}")
            }
        }
    }

    fun setMarkdownContent(newContent: String) {
        _state.value = MarkDownState.Success(newContent)
    }


}