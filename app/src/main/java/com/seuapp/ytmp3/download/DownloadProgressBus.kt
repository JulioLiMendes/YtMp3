package com.seuapp.ytmp3.download

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Canal simples de comunicação entre o [DownloadService] (que roda em
 * background/foreground) e a UI em Compose, sem precisar de bind ao
 * serviço. A UI apenas coleta [estado] com collectAsState().
 */
object DownloadProgressBus {

    private val _estado = MutableStateFlow<DownloadState>(DownloadState.Ocioso)
    val estado: StateFlow<DownloadState> = _estado.asStateFlow()

    fun atualizar(novoEstado: DownloadState) {
        _estado.value = novoEstado
    }

    fun resetar() {
        _estado.value = DownloadState.Ocioso
    }
}
