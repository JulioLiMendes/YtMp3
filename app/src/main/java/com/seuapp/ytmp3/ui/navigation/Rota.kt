package com.seuapp.ytmp3.ui.navigation

sealed class Rota(val caminho: String, val titulo: String) {
    data object Baixar : Rota("baixar", "Baixar")
    data object Biblioteca : Rota("biblioteca", "Biblioteca")
    data object Playlists : Rota("playlists", "Playlists")
    data object PlaylistDetalhes : Rota("playlist/{id}", "Playlist")
}
