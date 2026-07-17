package com.seuapp.ytmp3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seuapp.ytmp3.data.local.Playlist

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    termoBusca: String,
    aoMudarBusca: (String) -> Unit,
    aoClicarPlaylist: (Long, String) -> Unit,
    aoCriarPlaylist: (String) -> Unit,
    aoDeletarPlaylist: (Playlist) -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var nomeNovaPlaylist by remember { mutableStateOf("") }
    var playlistParaExcluir by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Criar Playlist")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Suas Playlists",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = termoBusca,
                        onValueChange = aoMudarBusca,
                        label = { Text("Buscar playlists") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                ListItem(
                    headlineContent = { Text("Músicas Favoritas") },
                    leadingContent = {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable { aoClicarPlaylist(-1L, "Favoritas") }
                )
                HorizontalDivider()
            }

            items(playlists, key = { it.id }) { playlist ->
                ListItem(
                    headlineContent = { Text(playlist.nome) },
                    leadingContent = {
                        Icon(Icons.Default.PlaylistPlay, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = { playlistParaExcluir = playlist }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir playlist",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.clickable { aoClicarPlaylist(playlist.id, playlist.nome) }
                )
                HorizontalDivider()
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nova Playlist") },
            text = {
                OutlinedTextField(
                    value = nomeNovaPlaylist,
                    onValueChange = { nomeNovaPlaylist = it },
                    label = { Text("Nome da playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nomeNovaPlaylist.isNotBlank()) {
                            aoCriarPlaylist(nomeNovaPlaylist)
                            nomeNovaPlaylist = ""
                            mostrarDialogo = false
                        }
                    }
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    playlistParaExcluir?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistParaExcluir = null },
            title = { Text("Excluir playlist?") },
            text = { Text("\"${playlist.nome}\" será excluída. As músicas continuam na sua biblioteca.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        aoDeletarPlaylist(playlist)
                        playlistParaExcluir = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
