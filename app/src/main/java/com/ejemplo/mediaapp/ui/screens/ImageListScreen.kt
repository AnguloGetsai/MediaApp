package com.ejemplo.mediaapp.ui.screens


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.ejemplo.mediaapp.viewmodel.MediaViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator

// --- 17. Screen 3: Carrusel de Imágenes ---
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ImageListScreen(mediaViewModel: MediaViewModel) {
    // Escuchar la lista de imágenes de la BD
    val imageList by mediaViewModel.allImages.collectAsState(initial = emptyList())

    // Estado del paginador (carrusel)
    val pagerState = rememberPagerState(pageCount = { imageList.size })

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mis Imágenes") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (imageList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay imágenes capturadas.")
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        // Ocupa el espacio restante para la imagen
                        .weight(1f),
                ) { page ->
                    val item = imageList[page]
                    // Coil para cargar la imagen desde la URI (content://...)
                    AsyncImage(
                        model = item.uri.toUri(),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                // Indicador de página
                if (pagerState.pageCount > 1) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = pagerState.pageCount,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                    )
                }
            }
        }
    }
}