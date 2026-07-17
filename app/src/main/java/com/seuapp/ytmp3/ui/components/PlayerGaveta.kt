package com.seuapp.ytmp3.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.seuapp.ytmp3.player.AudioBus
import com.seuapp.ytmp3.player.EstadoPlayer
import com.seuapp.ytmp3.util.FormatadorTempo

val ALTURA_PLAYER_COLAPSADA = 64.dp

/**
 * Gaveta persistente do player: fica sempre visível como uma barra
 * colapsada logo acima da navegação inferior (quando há música tocando),
 * e expande para tela cheia ao tocar ou arrastar para cima.
 *
 * [alturaNavBar] é usado para posicionar a barra colapsada exatamente
 * acima da NavigationBar, e para cobri-la por completo quando expandida.
 */
@Composable
fun PlayerGaveta(
    estado: EstadoPlayer,
    posicaoAtualMs: Long,
    expandido: Boolean,
    alturaNavBar: Dp,
    aoExpandir: () -> Unit,
    aoRecolher: () -> Unit,
    aoAlternarPlayPause: () -> Unit,
    aoProxima: () -> Unit,
    aoAnterior: () -> Unit,
    aoBuscarPosicao: (Long) -> Unit,
    aoFavoritar: () -> Unit,
    aoAlternarShuffle: () -> Unit,
    aoAdicionarNaPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val musica = estado.musicaAtual ?: return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val alturaTela = maxHeight

        val fracao by animateFloatAsState(
            targetValue = if (expandido) 1f else 0f,
            animationSpec = tween(280),
            label = "fracao_gaveta_player"
        )

        val alturaAtual = lerp(ALTURA_PLAYER_COLAPSADA, alturaTela, fracao)
        val deslocamentoY = lerp(-alturaNavBar, 0.dp, fracao)

        Surface(
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            shape = if (fracao > 0.9f) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(alturaAtual)
                .offset(y = deslocamentoY)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Crossfade(targetState = expandido, label = "conteudo_gaveta_player") { estaExpandido ->
                    if (estaExpandido) {
                        ConteudoPlayerCompleto(
                            estado = estado,
                            posicaoAtualMs = posicaoAtualMs,
                            aoRecolher = aoRecolher,
                            aoAlternarPlayPause = aoAlternarPlayPause,
                            aoProxima = aoProxima,
                            aoAnterior = aoAnterior,
                            aoBuscarPosicao = aoBuscarPosicao,
                            aoFavoritar = aoFavoritar,
                            aoAlternarShuffle = aoAlternarShuffle,
                            aoAdicionarNaPlaylist = aoAdicionarNaPlaylist
                        )
                    } else {
                        ConteudoPlayerColapsado(
                            estado = estado,
                            aoExpandir = aoExpandir,
                            aoAlternarPlayPause = aoAlternarPlayPause
                        )
                    }
                }
            }
        }
    }
}

/** Barra colapsada: toque ou arraste para cima para expandir. */
@Composable
private fun ConteudoPlayerColapsado(
    estado: EstadoPlayer,
    aoExpandir: () -> Unit,
    aoAlternarPlayPause: () -> Unit
) {
    val musica = estado.musicaAtual ?: return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTURA_PLAYER_COLAPSADA)
            .clickable { aoExpandir() }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, deslocamento ->
                    change.consume()
                    if (deslocamento < -12f) aoExpandir()
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = musica.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musica.titulo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = musica.artista ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = aoAlternarPlayPause) {
            Icon(
                imageVector = if (estado.tocando) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (estado.tocando) "Pausar" else "Tocar"
            )
        }
    }
}

/** Conteúdo em tela cheia: puxe a alça para baixo (ou toque na seta) para recolher. */
@Composable
private fun ConteudoPlayerCompleto(
    estado: EstadoPlayer,
    posicaoAtualMs: Long,
    aoRecolher: () -> Unit,
    aoAlternarPlayPause: () -> Unit,
    aoProxima: () -> Unit,
    aoAnterior: () -> Unit,
    aoBuscarPosicao: (Long) -> Unit,
    aoFavoritar: () -> Unit,
    aoAlternarShuffle: () -> Unit,
    aoAdicionarNaPlaylist: () -> Unit
) {
    val musica = estado.musicaAtual ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Alça de arrastar para recolher + botão de fallback (seta pra baixo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, deslocamento ->
                        change.consume()
                        if (deslocamento > 12f) aoRecolher()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .then(Modifier.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
            )
            IconButton(
                onClick = aoRecolher,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Recolher player")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            AsyncImage(
                model = musica.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = musica.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = musica.artista ?: "Artista desconhecido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                var arrastando by remember { mutableStateOf(false) }
                var posicaoArrastada by remember { mutableFloatStateOf(0f) }
                val duracao = estado.duracaoMs.coerceAtLeast(1L)
                val progresso = if (arrastando) posicaoArrastada else posicaoAtualMs.toFloat() / duracao

                Slider(
                    value = progresso.coerceIn(0f, 1f),
                    onValueChange = {
                        arrastando = true
                        posicaoArrastada = it
                    },
                    onValueChangeFinished = {
                        aoBuscarPosicao((posicaoArrastada * duracao).toLong())
                        arrastando = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(FormatadorTempo.formatar(posicaoAtualMs), style = MaterialTheme.typography.labelSmall)
                    Text(FormatadorTempo.formatar(estado.duracaoMs), style = MaterialTheme.typography.labelSmall)
                }
            }

            // Controles centralizados como na imagem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = aoAdicionarNaPlaylist) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = "Adicionar à Playlist", modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = aoAlternarShuffle) {
                    Icon(
                        imageVector = if (estado.shuffleMode) Icons.Filled.Shuffle else Icons.Outlined.Shuffle,
                        contentDescription = "Ordem Aleatória",
                        modifier = Modifier.size(24.dp),
                        tint = if (estado.shuffleMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                IconButton(onClick = aoAnterior, enabled = estado.temAnterior) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(32.dp))
                }
                FilledIconButton(onClick = aoAlternarPlayPause, modifier = Modifier.size(72.dp)) {
                    Icon(
                        imageVector = if (estado.tocando) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (estado.tocando) "Pausar" else "Tocar",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = aoProxima, enabled = estado.temProxima) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Próxima", modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = aoFavoritar) {
                    Icon(
                        imageVector = if (musica.favorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favoritar",
                        modifier = Modifier.size(24.dp),
                        tint = if (musica.favorito) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = estado.nomePlaylist ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                estado.proximaMusica?.let { proxima ->
                    Text(
                        text = "Próxima: ${proxima.titulo} - ${proxima.artista ?: "Desconhecido"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                BarraSomAnimada(tocando = estado.tocando)
            }
        }
    }
}

@Composable
private fun BarraSomAnimada(tocando: Boolean) {
    val amplitudeReal by AudioBus.amplitude.collectAsStateWithLifecycle()

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(40.dp)
    ) {
        repeat(12) { index ->
            // Fator individual para cada barra (simula um equalizador partir da amplitude total)
            val fatorIndividual = remember { (4..10).random() / 10f }

            val alturaAlvo = if (tocando) {
                (amplitudeReal * fatorIndividual).coerceIn(0.1f, 1f)
            } else {
                0.2f
            }

            val alturaAnimada by animateFloatAsState(
                targetValue = alturaAlvo,
                animationSpec = tween(150), // Sincronização rápida
                label = "barra_$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp * alturaAnimada)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (tocando) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    }
}
