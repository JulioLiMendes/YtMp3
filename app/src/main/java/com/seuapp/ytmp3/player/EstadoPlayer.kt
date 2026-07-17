package com.seuapp.ytmp3.player

import com.seuapp.ytmp3.data.local.Musica

/**
 * Snapshot do estado do player em um dado momento, pronto para a UI consumir.
 */
data class EstadoPlayer(
    val musicaAtual: Musica? = null,
    val proximaMusica: Musica? = null,
    val tocando: Boolean = false,
    val posicaoMs: Long = 0L,
    val duracaoMs: Long = 0L,
    val temProxima: Boolean = false,
    val temAnterior: Boolean = false,
    val shuffleMode: Boolean = false,
    val nomePlaylist: String? = null
)
