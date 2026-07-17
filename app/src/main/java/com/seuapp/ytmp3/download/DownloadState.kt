package com.seuapp.ytmp3.download

import com.seuapp.ytmp3.data.local.Musica

/**
 * Representa o estado atual de um download em andamento.
 * A UI observa isso (via [DownloadProgressBus]) para mostrar
 * barra de progresso, mensagens de erro, etc.
 */
sealed class DownloadState {
    data object Ocioso : DownloadState()
    data object BuscandoInformacoes : DownloadState()
    data class Baixando(val progresso: Float, val etaSegundos: Long) : DownloadState()
    data object Convertendo : DownloadState()
    data class Concluido(val musica: Musica) : DownloadState()
    data class Erro(val mensagem: String) : DownloadState()
}
