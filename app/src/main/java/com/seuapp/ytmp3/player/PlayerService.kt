package com.seuapp.ytmp3.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.seuapp.ytmp3.MainActivity

/**
 * Serviço que hospeda o ExoPlayer e expõe uma [MediaSession].
 *
 * Ao usar MediaSessionService, o Media3 cuida automaticamente de:
 * - Notificação de reprodução (play/pause/next/prev) na tela de bloqueio
 * - Integração com fones bluetooth, Android Auto, Wear OS
 * - Manter o áudio tocando em segundo plano como foreground service
 *
 * A UI nunca fala diretamente com este serviço: ela se conecta através
 * de um MediaController (ver [PlayerController]), então este service
 * não expõe métodos próprios além do que o framework exige.
 */
class PlayerService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        // Configura o visualizador de áudio
        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(TeeAudioProcessor(VisualizadorSink())))
                    .build()
            }
        }

        player = ExoPlayer.Builder(this, renderersFactory)
            .setHandleAudioBecomingNoisy(true) // pausa sozinho ao desconectar o fone
            .build()

        // Ao tocar na notificação, abre a MainActivity
        val intentAbrirApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(intentAbrirApp)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * Se o usuário fechar o app pela lista de recentes e nada estiver
     * tocando (ou não estiver com playWhenReady), encerramos o serviço
     * para não ficar consumindo recursos à toa.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val sessaoAtual = mediaSession ?: return
        if (!sessaoAtual.player.playWhenReady || sessaoAtual.player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
