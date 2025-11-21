package com.ejemplo.mediaapp.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import com.ejemplo.mediaapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.media.MediaMetadataRetriever
import java.io.File

class MediaViewModel(
    application: Application,
    private val repository: MediaRepository
) : AndroidViewModel(application) {

    val allAudio: StateFlow<List<MediaItem>> = repository.getAllAudio()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allImages: StateFlow<List<MediaItem>> = repository.getAllImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<MediaItem>> = repository.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Inserta media desde una URI **/
    fun insertMediaFromUri(uri: Uri, type: MediaType) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val metadata = getMetadataFromUri(context.contentResolver, uri)

                val item = MediaItem(
                    uri = uri.toString(),
                    name = metadata.first,
                    date = System.currentTimeMillis(),
                    duration = metadata.second,
                    type = type
                )

                repository.insertMedia(item)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Inserta media desde un archivo File **/
    fun insertMediaFromFile(file: File, type: MediaType) {
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>().applicationContext
                val authority = "${ctx.packageName}.fileprovider"

                val uri = FileProvider.getUriForFile(ctx, authority, file)

                insertMediaFromUri(uri, type)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Obtiene nombre y duración **/
    private fun getMetadataFromUri(contentResolver: ContentResolver, uri: Uri): Pair<String, Long> {
        var name = "Unknown"
        var duration = 0L

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = cursor.getString(index)
            }
        }

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(getApplication(), uri)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durStr?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (e: Exception) {
            duration = 0L
        }

        return Pair(name, duration)
    }
}