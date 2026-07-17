package com.seuapp.ytmp3.download

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.seuapp.ytmp3.data.local.Musica
import com.seuapp.ytmp3.data.repository.MusicaRepository
import com.seuapp.ytmp3.util.NotificationHelper
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Serviço em foreground que executa o pipeline completo de download:
 *
 * 1. Extrai metadados do vídeo (título, autor, duração, thumbnail)
 * 2. Baixa o áudio e converte para mp3 (via yt-dlp + ffmpeg embutidos)
 * 3. Salva o arquivo na pasta própria do app
 * 4. Persiste os metadados no Room através do [MusicaRepository]
 *
 * O progresso é publicado no [DownloadProgressBus] para a UI observar,
 * e refletido também na notificação (obrigatória por ser foreground service).
 */
class DownloadService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var repository: MusicaRepository

    override fun onCreate() {
        super.onCreate()
        repository = MusicaRepository(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Sobe como foreground imediatamente (obrigatório em poucos segundos)
        startForeground(NotificationHelper.ID_NOTIFICACAO_DOWNLOAD, criarNotificacaoBase())

        scope.launch {
            processarDownload(url)
        }

        return START_NOT_STICKY
    }

    private suspend fun processarDownload(url: String) {
        try {
            DownloadProgressBus.atualizar(DownloadState.BuscandoInformacoes)
            atualizarNotificacao("Buscando informações do vídeo...", indeterminado = true)

            val info = YoutubeDL.getInstance().getInfo(url)
            val idVideo = info.id ?: UUID.randomUUID().toString()

            val pastaMusicas = repository.obterPastaMusicas()
            val templateSaida = File(pastaMusicas, "$idVideo.%(ext)s").absolutePath

            val request = YoutubeDLRequest(url).apply {
                addOption("-x") // extrai apenas o áudio
                addOption("--audio-format", "mp3")
                addOption("--audio-quality", "0") // melhor qualidade de mp3 disponível
                addOption("-o", templateSaida)
            }

            DownloadProgressBus.atualizar(DownloadState.Baixando(0f, 0L))

            YoutubeDL.getInstance().execute(request, idVideo) { progresso, etaSegundos, _ ->
                DownloadProgressBus.atualizar(DownloadState.Baixando(progresso, etaSegundos))
                atualizarNotificacao(
                    "Baixando: ${progresso.toInt()}%",
                    indeterminado = false,
                    progresso = progresso.toInt()
                )
            }

            DownloadProgressBus.atualizar(DownloadState.Convertendo)
            atualizarNotificacao("Convertendo para mp3...", indeterminado = true)

            val arquivoFinal = File(pastaMusicas, "$idVideo.mp3")
            if (!arquivoFinal.exists()) {
                throw IllegalStateException("Arquivo mp3 não encontrado após a conversão")
            }

            val duracaoMs = obterDuracaoMs(arquivoFinal)

            val musica = Musica(
                id = idVideo,
                titulo = info.title ?: "Título desconhecido",
                artista = info.uploader,
                duracaoMs = duracaoMs,
                caminhoArquivo = arquivoFinal.absolutePath,
                thumbnailUrl = info.thumbnail
            )

            repository.salvar(musica)

            DownloadProgressBus.atualizar(DownloadState.Concluido(musica))
            atualizarNotificacao("Download concluído: ${musica.titulo}", indeterminado = false, progresso = 100)
        } catch (e: Exception) {
            DownloadProgressBus.atualizar(DownloadState.Erro(e.message ?: "Erro desconhecido ao baixar"))
            atualizarNotificacao("Erro ao baixar: ${e.message}", indeterminado = false)
        } finally {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun obterDuracaoMs(arquivo: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(arquivo.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }

    private fun criarNotificacaoBase(): Notification {
        return NotificationCompat.Builder(this, NotificationHelper.CANAL_DOWNLOAD)
            .setContentTitle("YtMp3")
            .setContentText("Preparando download...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }

    private fun atualizarNotificacao(texto: String, indeterminado: Boolean, progresso: Int = 0) {
        val notificacao = NotificationCompat.Builder(this, NotificationHelper.CANAL_DOWNLOAD)
            .setContentTitle("YtMp3")
            .setContentText(texto)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progresso, indeterminado)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NotificationHelper.ID_NOTIFICACAO_DOWNLOAD, notificacao)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_URL = "extra_url"

        /**
         * Ponto de entrada único para iniciar um download.
         * Chame isso a partir da UI passando o link do YouTube colado pelo usuário.
         */
        fun iniciar(context: Context, url: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
            context.startForegroundService(intent)
        }
    }
}
