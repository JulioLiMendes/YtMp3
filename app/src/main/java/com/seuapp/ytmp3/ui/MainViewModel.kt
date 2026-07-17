package com.seuapp.ytmp3.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seuapp.ytmp3.data.local.Musica
import com.seuapp.ytmp3.data.local.Playlist
import com.seuapp.ytmp3.data.repository.MusicaRepository
import com.seuapp.ytmp3.download.DownloadProgressBus
import com.seuapp.ytmp3.download.DownloadService
import com.seuapp.ytmp3.download.DownloadState
import com.seuapp.ytmp3.player.EstadoPlayer
import com.seuapp.ytmp3.player.PlayerController
import com.seuapp.ytmp3.ui.models.ListaMusicasEstavel
import com.seuapp.ytmp3.util.UrlValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicaRepository(application)
    private val playerController = PlayerController(application)

    private val _termoBusca = MutableStateFlow("")
    val termoBusca: StateFlow<String> = _termoBusca.asStateFlow()

    private val _termoBuscaPlaylists = MutableStateFlow("")
    val termoBuscaPlaylists: StateFlow<String> = _termoBuscaPlaylists.asStateFlow()

    val musicas: StateFlow<List<Musica>> = _termoBusca
        .flatMapLatest { termo ->
            if (termo.isBlank()) {
                repository.listarTodas()
            } else {
                repository.buscar(termo)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val musicasEstaveis: StateFlow<ListaMusicasEstavel> = musicas
        .map { ListaMusicasEstavel(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListaMusicasEstavel())

    val favoritas: StateFlow<List<Musica>> = repository.listarFavoritas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = _termoBuscaPlaylists
        .flatMapLatest { termo ->
            if (termo.isBlank()) {
                repository.listarPlaylists()
            } else {
                repository.buscarPlaylists(termo)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadState: StateFlow<DownloadState> = DownloadProgressBus.estado

    val playerState: StateFlow<EstadoPlayer> = playerController.estado

    // Media3 não emite posição continuamente, então fazemos polling
    // simples enquanto a música está tocando (usado pela barra de progresso).
    private val _posicaoAtualMs = MutableStateFlow(0L)
    val posicaoAtualMs: StateFlow<Long> = _posicaoAtualMs

    private var tickerJob: Job? = null

    init {
        playerController.conectar {
            iniciarTickerDePosicao()
        }
    }

    private fun iniciarTickerDePosicao() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                _posicaoAtualMs.value = playerController.posicaoAtualMs()
                delay(500)
            }
        }
    }

    /** Retorna false se o link não for um link válido do YouTube. */
    fun baixar(url: String): Boolean {
        if (!UrlValidator.ehLinkYoutubeValido(url)) return false
        DownloadService.iniciar(getApplication(), url)
        return true
    }

    fun resetarEstadoDownload() = DownloadProgressBus.resetar()

    fun buscarMusica(termo: String) {
        _termoBusca.value = termo
    }

    fun buscarPlaylists(termo: String) {
        _termoBuscaPlaylists.value = termo
    }

    fun renomearMusica(musica: Musica, novoTitulo: String) = viewModelScope.launch {
        repository.atualizar(musica.copy(titulo = novoTitulo))
    }

    fun alternarFavorito(musica: Musica) = viewModelScope.launch {
        repository.alternarFavorito(musica)
    }

    fun remover(musica: Musica) = viewModelScope.launch {
        repository.remover(musica)
    }

    /** Toca a música clicada, usando a lista visível (biblioteca ou favoritos) como fila de reprodução. */
    fun tocar(musica: Musica, listaAtual: List<Musica>, nomePlaylist: String? = null) {
        val indice = listaAtual.indexOfFirst { it.id == musica.id }.coerceAtLeast(0)
        playerController.tocarLista(listaAtual, indice, nomePlaylist)
    }

    fun alternarPlayPause() = playerController.alternarPlayPause()
    fun proxima() = playerController.proxima()
    fun anterior() = playerController.anterior()
    fun buscarPosicao(posicaoMs: Long) = playerController.buscarPosicao(posicaoMs)
    fun alternarShuffle() = playerController.alternarShuffle()

    // Operações de Playlist
    fun criarPlaylist(nome: String) = viewModelScope.launch {
        repository.criarPlaylist(nome)
    }

    fun deletarPlaylist(playlist: Playlist) = viewModelScope.launch {
        repository.deletarPlaylist(playlist)
    }

    fun adicionarNaPlaylist(playlistId: Long, musicaId: String) = viewModelScope.launch {
        if (playlistId == -1L) {
            repository.definirFavorito(musicaId, true)
        } else {
            repository.adicionarMusicaNaPlaylist(playlistId, musicaId)
        }
    }

    fun removerDaPlaylist(playlistId: Long, musicaId: String) = viewModelScope.launch {
        if (playlistId == -1L) {
            repository.definirFavorito(musicaId, false)
        } else {
            repository.removerMusicaDaPlaylist(playlistId, musicaId)
        }
    }

    fun listarMusicasDaPlaylist(playlistId: Long): Flow<List<Musica>> {
        return if (playlistId == -1L) {
            repository.listarFavoritas()
        } else {
            repository.listarMusicasDaPlaylist(playlistId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        playerController.desconectar()
    }
}
