package com.seuapp.ytmp3.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.seuapp.ytmp3.data.local.Musica
import com.seuapp.ytmp3.ui.components.EstadoVazio
import com.seuapp.ytmp3.ui.components.MusicaItem
import com.seuapp.ytmp3.ui.models.ListaMusicasEstavel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetalhesScreen(
    titulo: String,
    musicas: ListaMusicasEstavel,
    aoVoltar: () -> Unit,
    aoTocar: (Musica) -> Unit,
    aoFavoritar: (Musica) -> Unit,
    aoAdicionarMusica: () -> Unit,
    aoRemoverDaPlaylist: (Musica) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = aoAdicionarMusica) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Música")
                    }
                }
            )
        }
    ) { padding ->
        if (musicas.itens.isEmpty()) {
            EstadoVazio("Esta playlist está vazia.")
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(
                    items = musicas.itens,
                    key = { it.id },
                    contentType = { "musica" }
                ) { musica ->
                    MusicaItem(
                        musica = musica,
                        aoClicar = { aoTocar(musica) },
                        aoFavoritar = { aoFavoritar(musica) },
                        aoRemover = { aoRemoverDaPlaylist(musica) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
