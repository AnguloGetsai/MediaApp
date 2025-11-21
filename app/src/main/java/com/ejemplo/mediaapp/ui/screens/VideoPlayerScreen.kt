package com.ejemplo.mediaapp.ui.screens




import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import com.ejemplo.mediaapp.viewmodel.PlaybackViewModel


// --- 19. Screen 5: Reproductor de Video ---
@Composable
fun VideoPlayerScreen(
    uri: String, // Recibimos la Uri decodificada como String
    playbackViewModel: PlaybackViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Obtener la instancia del player desde el ViewModel
    val exoPlayer = playbackViewModel.exoPlayer

    // Inicia la reproducción cuando el Composable entra en la pantalla
    LaunchedEffect(uri) {
        playbackViewModel.playMedia(uri)
    }

    // Manejo del ciclo de vida para pausar la reproducción en segundo plano
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // Pausar si la app se va a segundo plano (ON_PAUSE/ON_STOP)
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                // Reanudar si la app vuelve al primer plano
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                // La liberación se maneja en el onCleared del ViewModel, pero es bueno
                // asegurarnos de pausar.
                Lifecycle.Event.ON_DESTROY -> { /* Nada que hacer aquí, el VM es el responsable */ }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Pausar el reproductor cuando el composable sale de la composición
            exoPlayer.pause()
        }
    }

    // Usamos AndroidView para mostrar el PlayerView de ExoPlayer (un View tradicional)
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                // Crea una instancia del PlayerView de ExoPlayer
                PlayerView(context).apply {
                    // Asigna la instancia de ExoPlayer del ViewModel
                    player = exoPlayer
                    // Muestra/oculta controles
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}