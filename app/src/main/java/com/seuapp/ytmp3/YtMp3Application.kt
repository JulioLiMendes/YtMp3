package com.seuapp.ytmp3

import android.app.Application
import android.util.Log
import com.seuapp.ytmp3.util.NotificationHelper
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class YtMp3Application : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializa os binários de yt-dlp e ffmpeg empacotados no app.
        // Isso é necessário apenas uma vez, aqui na Application.
        try {
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
            
            // Tenta atualizar o binário do yt-dlp em background para evitar erros de "sign in" ou mudanças no YouTube
            Thread {
                try {
                    YoutubeDL.getInstance().updateYoutubeDL(this)
                } catch (e: Exception) {
                    Log.e("YtMp3Application", "Falha ao atualizar yt-dlp", e)
                }
            }.start()
            
        } catch (e: YoutubeDLException) {
            Log.e("YtMp3Application", "Falha ao inicializar YoutubeDL/FFmpeg", e)
        }

        NotificationHelper.criarCanais(this)
    }
}
