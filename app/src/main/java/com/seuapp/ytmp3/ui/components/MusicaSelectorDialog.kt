package com.seuapp.ytmp3.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seuapp.ytmp3.data.local.Musica

@Composable
fun MusicaSelectorDialog(
    musicas: List<Musica>,
    onDismiss: () -> Unit,
    onMusicaSelected: (Musica) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar à Playlist") },
        text = {
            if (musicas.isEmpty()) {
                Text("Nenhuma música disponível na biblioteca.")
            } else {
                LazyColumn {
                    items(musicas, key = { it.id }) { musica ->
                        ListItem(
                            headlineContent = { Text(musica.titulo) },
                            supportingContent = { Text(musica.artista ?: "Artista desconhecido") },
                            leadingContent = {
                                AsyncImage(
                                    model = musica.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMusicaSelected(musica) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
