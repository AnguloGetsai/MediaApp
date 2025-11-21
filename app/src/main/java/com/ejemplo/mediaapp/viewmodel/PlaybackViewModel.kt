package com.ejemplo.mediaapp.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ejemplo.mediaapp.data.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaybackViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application), SensorEventListener {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val sensorManager =
        getSystemService(application, SensorManager::class.java) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isAccelerometerEnabled = MutableStateFlow(false)
    val isAccelerometerEnabled: StateFlow<Boolean> = _isAccelerometerEnabled.asStateFlow()

    val currentVolume: StateFlow<Float> = settingsRepository.userVolume.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsRepository.DEFAULT_VOLUME
    )

    init {
        viewModelScope.launch {
            currentVolume.collect { vol ->
                exoPlayer.volume = vol
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun playMedia(uri: String) {
        val item = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(item)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause()
        else exoPlayer.play()
    }

    fun releasePlayer() {
        exoPlayer.release()
        unregisterSensor()
    }

    fun toggleAccelerometer() {
        _isAccelerometerEnabled.update { enabled ->
            if (!enabled) registerSensor()
            else unregisterSensor()
            !enabled
        }
    }

    private fun registerSensor() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val y = event.values[1]
            val newVol = ((y + 5f) / 10f).coerceIn(0f, 1f)
            setVolume(newVol)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setVolume(v: Float) {
        val vol = v.coerceIn(0f, 1f)
        exoPlayer.volume = vol

        viewModelScope.launch {
            settingsRepository.saveVolume(vol)
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}