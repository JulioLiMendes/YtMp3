package com.seuapp.ytmp3.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val CANAL_DOWNLOAD = "canal_download"
    const val CANAL_PLAYBACK = "canal_playback"

    const val ID_NOTIFICACAO_DOWNLOAD = 1001
    const val ID_NOTIFICACAO_PLAYBACK = 1002

    fun criarCanais(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val canalDownload = NotificationChannel(
            CANAL_DOWNLOAD,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Progresso dos downloads de música"
        }

        val canalPlayback = NotificationChannel(
            CANAL_PLAYBACK,
            "Reprodução",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Controles do player de música"
        }

        manager.createNotificationChannel(canalDownload)
        manager.createNotificationChannel(canalPlayback)
    }
}
