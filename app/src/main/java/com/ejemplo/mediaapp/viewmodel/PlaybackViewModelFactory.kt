package com.ejemplo.mediaapp.viewmodel


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ejemplo.mediaapp.data.SettingsRepository

class PlaybackViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(PlaybackViewModel::class.java)) {

            val repo = SettingsRepository(application)

            @Suppress("UNCHECKED_CAST")
            return PlaybackViewModel(application, repo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}