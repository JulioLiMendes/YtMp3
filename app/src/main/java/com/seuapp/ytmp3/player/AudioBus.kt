package com.seuapp.ytmp3.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Barramento simples para transmitir a amplitude do áudio em tempo real
 * do PlayerService para a UI do Compose.
 */
object AudioBus {
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun atualizar(valor: Float) {
        _amplitude.value = valor.coerceIn(0f, 1f)
    }
}
