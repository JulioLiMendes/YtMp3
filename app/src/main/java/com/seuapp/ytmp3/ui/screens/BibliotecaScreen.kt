package com.seuapp.ytmp3.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seuapp.ytmp3.data.local.Musica
import com.seuapp.ytmp3.ui.components.EstadoVazio
import com.seuapp.ytmp3.ui.components.MusicaItem
import com.seuapp.ytmp3.ui.models.ListaMusicasEstavel

@Composable
fun BibliotecaScreen(
    musicas: ListaMusicasEstavel,
    termoBusca: String,
    aoMudarBusca: (String) -> Unit,
    aoTocar: (Musica) -> Unit,
    aoFavoritar: (Musica) -> Unit,
    aoRemover: (Musica) -> Unit,
    aoAdicionarNaPlaylist: (Musica) -> Unit,
    aoRenomear: (Musica, String) -> Unit
) {
    var musicaParaRemover by remember { mutableStateOf<Musica?>(null) }
    var musicaParaRenomear by remember { mutableStateOf<Musica?>(null) }
    var novoNome by remember { mutableStateOf("") }

    if (musicas.itens.isEmpty() && termoBusca.isBlank()) {
        EstadoVazio("Nenhuma música baixada ainda.\nVá em \"Baixar\" e cole um link do YouTube.")
        return
    }

    LazyColumn {
        item {
            OutlinedTextField(
                value = termoBusca,
                onValueChange = aoMudarBusca,
                label = { Text("Buscar na biblioteca") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        items(
            items = musicas.itens,
            key = { it.id },
            contentType = { "musica" }
        ) { musica ->
            MusicaItem(
                musica = musica,
                aoClicar = { aoTocar(musica) },
                aoFavoritar = { aoFavoritar(musica) },
                aoRemover = { musicaParaRemover = musica },
                aoAdicionarNaPlaylist = { aoAdicionarNaPlaylist(musica) },
                aoRenomear = {
                    musicaParaRenomear = musica
                    novoNome = musica.titulo
                }
            )
            HorizontalDivider()
        }
    }

    if (musicaParaRenomear != null) {
        AlertDialog(
            onDismissRequest = { musicaParaRenomear = null },
            title = { Text("Renomear música") },
            text = {
                OutlinedTextField(
                    value = novoNome,
                    onValueChange = { novoNome = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (novoNome.isNotBlank()) {
                            musicaParaRenomear?.let { aoRenomear(it, novoNome) }
                            musicaParaRenomear = null
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { musicaParaRenomear = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (musicaParaRemover != null) {
        AlertDialog(
            onDismissRequest = { musicaParaRemover = null },
            title = { Text("Remover música?") },
            text = { Text("Deseja remover \"${musicaParaRemover?.titulo}\"? O arquivo também será apagado do seu dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        musicaParaRemover?.let { aoRemover(it) }
                        musicaParaRemover = null
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { musicaParaRemover = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
