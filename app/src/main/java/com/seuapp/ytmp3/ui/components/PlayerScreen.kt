package com.seuapp.ytmp3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seuapp.ytmp3.player.EstadoPlayer
import com.seuapp.ytmp3.util.FormatadorTempo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    estado: EstadoPlayer,
    posicaoAtualMs: Long,
    aoFechar: () -> Unit,
    aoAlternarPlayPause: () -> Unit,
    aoProxima: () -> Unit,
    aoAnterior: () -> Unit,
    aoBuscarPosicao: (Long) -> Unit,
    aoFavoritar: () -> Unit,
    aoAlternarShuffle: () -> Unit,
    aoAdicionarNaPlaylist: () -> Unit
) {
    val musica = estado.musicaAtual ?: return

    ModalBottomSheet(onDismissRequest = aoFechar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (AsyncImage and Slider part remain same)
            AsyncImage(
                model = musica.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Enquanto o usuário arrasta o slider, exibimos a posição arrastada
            // em vez da posição real (que continua chegando via polling).
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = aoAdicionarNaPlaylist) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Adicionar à Playlist"
                    )
                }
                IconButton(onClick = aoAlternarShuffle) {
                    Icon(
                        imageVector = if (estado.shuffleMode) Icons.Filled.Shuffle else Icons.Outlined.Shuffle,
                        contentDescription = "Ordem Aleatória",
                        tint = if (estado.shuffleMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                IconButton(onClick = aoAnterior, enabled = estado.temAnterior) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Anterior",
                        modifier = Modifier.size(36.dp)
                    )
                }
                FilledIconButton(onClick = aoAlternarPlayPause, modifier = Modifier.size(64.dp)) {
                    Icon(
                        imageVector = if (estado.tocando) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (estado.tocando) "Pausar" else "Tocar",
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = aoProxima, enabled = estado.temProxima) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Próxima",
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = aoFavoritar) {
                    Icon(
                        imageVector = if (musica.favorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (musica.favorito) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
