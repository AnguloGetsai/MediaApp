package com.ejemplo.mediaapp

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// --- 21. Utilidades, colocar en paquete principal —

/**
 * Convierte una duración en milisegundos a formato "MM:SS".
 */
fun formatDuration(millis: Long): String {
    if (millis <= 0) return "00:00"
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        // Minutos
        TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
        // Segundos
        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
    )
}

 
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}