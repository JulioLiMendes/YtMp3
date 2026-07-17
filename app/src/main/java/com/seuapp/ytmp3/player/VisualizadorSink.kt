package com.seuapp.ytmp3.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * Intercepta o buffer de áudio do ExoPlayer para calcular a amplitude (volume) atual.
 */
@UnstableApi
class VisualizadorSink : TeeAudioProcessor.AudioBufferSink {

    override fun flush(sampleRateHz: Int, channelCount: Int, encoding: Int) {
        // Nada a fazer no flush
    }

    override fun handleBuffer(buffer: ByteBuffer) {
        // Sem os parâmetros de encoding/channel nesta versão da interface, 
        // assumimos o padrão do ExoPlayer (16bit PCM) para o cálculo.
        
        val data = buffer.asShortBuffer()
        var sum = 0.0
        val count = data.remaining()
        
        if (count == 0) return

        while (data.hasRemaining()) {
            val sample = data.get().toDouble()
            sum += sample * sample
        }

        // Calcula o RMS (Root Mean Square)
        val rms = sqrt(sum / count)
        
        // Normaliza para 0.0 - 1.0 (Short.MAX_VALUE é 32767)
        val amplitude = (rms / 32768.0).toFloat() * 1.5f
        
        AudioBus.atualizar(amplitude)
    }
}
