package com.seuapp.ytmp3.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.seuapp.ytmp3.data.local.Musica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ponte entre a UI (Compose/ViewModel) e o [PlayerService].
 *
 * A UI nunca acessa o ExoPlayer diretamente — sempre passa por aqui.
 * Isso mantém a lógica de player isolada e fácil de testar/trocar depois.
 *
 * Uso típico (dentro de um ViewModel):
 *   val playerController = PlayerController(context)
 *   playerController.conectar()
 *   ...
 *   playerController.tocarLista(minhasMusicas, indiceClicado)
 *   ...
 *   playerController.desconectar() // no onCleared()
 */
class PlayerController(private val context: Context) {

    private var controller: MediaController? = null

    private val _estado = MutableStateFlow(EstadoPlayer())
    val estado: StateFlow<EstadoPlayer> = _estado.asStateFlow()

    // Guardamos a fila atual de Musica (não só MediaItem) para conseguir
    // devolver o objeto completo (thumbnail, favorito, etc.) para a UI
    // quando o player troca de faixa.
    private var filaAtual: List<Musica> = emptyList()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _estado.value = _estado.value.copy(tocando = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            atualizarMusicaAtual(mediaItem)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                atualizarMusicaAtual(controller?.currentMediaItem)
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _estado.value = _estado.value.copy(shuffleMode = shuffleModeEnabled)
            atualizarMusicaAtual(controller?.currentMediaItem)
        }
    }

    private fun atualizarMusicaAtual(mediaItem: MediaItem?) {
        val c = controller ?: return
        val musica = filaAtual.find { it.id == mediaItem?.mediaId }
        
        val proximoIndice = c.nextMediaItemIndex
        val proximaMusica = if (proximoIndice != C.INDEX_UNSET) {
            val proximoMediaItem = c.getMediaItemAt(proximoIndice)
            filaAtual.find { it.id == proximoMediaItem.mediaId }
        } else {
            null
        }

        _estado.value = _estado.value.copy(
            musicaAtual = musica,
            proximaMusica = proximaMusica,
            duracaoMs = musica?.duracaoMs ?: 0L,
            temProxima = c.hasNextMediaItem(),
            temAnterior = c.hasPreviousMediaItem(),
            shuffleMode = c.shuffleModeEnabled
        )
    }

    /** Conecta ao PlayerService. Chame na criação da tela/ViewModel. */
    fun conectar(aoConectar: () -> Unit = {}) {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            aoConectar()
        }, MoreExecutors.directExecutor())
    }

    /** Desconecta do serviço. Chame no onCleared() do ViewModel. */
    fun desconectar() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    /** Toca uma lista de músicas a partir do índice escolhido (ex: playlist / biblioteca / favoritos). */
    fun tocarLista(musicas: List<Musica>, indiceInicial: Int, nomePlaylist: String? = null) {
        if (musicas.isEmpty()) return
        filaAtual = musicas
        _estado.value = _estado.value.copy(nomePlaylist = nomePlaylist)
        val itens = MusicaMediaItemMapper.paraListaMediaItems(musicas)
        controller?.apply {
            setMediaItems(itens, indiceInicial, 0L)
            prepare()
            play()
        }
    }

    /** Toca uma única música imediatamente (ex: clique direto num item da lista de favoritos). */
    fun tocarUnica(musica: Musica) = tocarLista(listOf(musica), 0)

    fun alternarPlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun alternarShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun proxima() {
        controller?.seekToNextMediaItem()
    }

    fun anterior() {
        controller?.seekToPreviousMediaItem()
    }

    fun buscarPosicao(posicaoMs: Long) {
        controller?.seekTo(posicaoMs)
    }

    /** Posição atual em ms. Útil para polling periódico (ex: atualizar a barra de progresso a cada 500ms). */
    fun posicaoAtualMs(): Long = controller?.currentPosition ?: 0L
}
