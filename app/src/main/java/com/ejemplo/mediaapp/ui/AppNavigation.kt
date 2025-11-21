package com.ejemplo.mediaapp.ui

import com.ejemplo.mediaapp.viewmodel.PlaybackViewModel
import com.ejemplo.mediaapp.viewmodel.PlaybackViewModelFactory

import com.ejemplo.mediaapp.ui.screens.RecordingScreen
import com.ejemplo.mediaapp.ui.screens.AudioListScreen
import com.ejemplo.mediaapp.ui.screens.ImageListScreen
import com.ejemplo.mediaapp.ui.screens.VideoListScreen
import com.ejemplo.mediaapp.ui.screens.VideoPlayerScreen


import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejemplo.mediaapp.data.AudioRecorder
import com.ejemplo.mediaapp.viewmodel.MediaViewModel
import com.ejemplo.mediaapp.viewmodel.MediaViewModelFactory


// --- 11. Definición de Rutas de Navegación ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Recording : Screen("recording", "Grabar", Icons.Default.Mic)
    object AudioList : Screen("audio", "Audios", Icons.Default.AudioFile)
    object ImageList : Screen("images", "Imágenes", Icons.Default.Image)
    object VideoList : Screen("videos", "Videos", Icons.Default.Videocam)
    // Pantalla de detalle (no va en la barra de navegación)
    object VideoPlayer : Screen("video_player/{uri}", "Video Player", Icons.Default.Videocam) {
        fun createRoute(uri: String) = "video_player/$uri"
    }
}
val navBarItems = listOf(
    Screen.Recording,
    Screen.AudioList,
    Screen.ImageList,
    Screen.VideoList,
)

// --- 12. Navegación ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    // Asegúrate de que el contexto es de tipo Application para los ViewModels
    val application = context.applicationContext as Application
    // Factories para los ViewModels
    val mediaViewModel: MediaViewModel = viewModel(
        factory = MediaViewModelFactory(application)
    )
    val playbackViewModel: PlaybackViewModel = viewModel(
        factory = PlaybackViewModelFactory(application)
    )
    // Inicializar el AudioRecorder aquí y pasarlo a la pantalla de grabación
    val audioRecorder = AudioRecorder(context)

    Scaffold(
        bottomBar = {
            AppBottomNavBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Recording.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Recording.route) {
                RecordingScreen(
                    mediaViewModel = mediaViewModel,
                    audioRecorder = audioRecorder // Pasamos el helper de grabación
                )
            }
            composable(Screen.AudioList.route) {
                AudioListScreen(
                    mediaViewModel = mediaViewModel,
                    playbackViewModel = playbackViewModel
                )
            }
            composable(Screen.ImageList.route) {
                ImageListScreen(mediaViewModel = mediaViewModel)
            }
            composable(Screen.VideoList.route) {
                VideoListScreen(
                    mediaViewModel = mediaViewModel,
                    navController = navController
                )
            }
            composable(
                route = Screen.VideoPlayer.route,
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uri = backStackEntry.arguments?.getString("uri")
                if (uri != null) {
                    VideoPlayerScreen(
                        // Usar Uri.decode para decodificar la Uri que se codificó al navegar
                        uri = Uri.decode(uri),
                        playbackViewModel = playbackViewModel
                    )
                }
            }
        }

    }
}

// --- 13. Barra de Navegación ---
@Composable
fun AppBottomNavBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        navBarItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}