package com.seuapp.ytmp3.util

import java.util.concurrent.TimeUnit

object FormatadorTempo {
    fun formatar(ms: Long): String {
        val totalSegundos = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        return String.format("%d:%02d", minutos, segundos)
    }
}
