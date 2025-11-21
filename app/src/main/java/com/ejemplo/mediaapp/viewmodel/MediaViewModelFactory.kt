package com.ejemplo.mediaapp.viewmodel


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ejemplo.mediaapp.data.AppDatabase
import com.ejemplo.mediaapp.data.MediaRepository

class MediaViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {

            val database = AppDatabase.getDatabase(application)
            val repo = MediaRepository(database.mediaDao())

            @Suppress("UNCHECKED_CAST")
            return MediaViewModel(application, repo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}