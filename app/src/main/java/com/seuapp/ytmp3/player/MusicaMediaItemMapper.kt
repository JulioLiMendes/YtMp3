package com.seuapp.ytmp3.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.seuapp.ytmp3.data.local.Musica
import java.io.File

object MusicaMediaItemMapper {

    /**
     * O mediaId usado aqui é o mesmo id da Musica no Room — assim,
     * ao receber eventos de troca de faixa do player, conseguimos
     * achar de volta o registro completo (ver [PlayerController]).
     */
    fun paraMediaItem(musica: Musica): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(musica.titulo)
            .setArtist(musica.artista ?: "Artista desconhecido")
            .setArtworkUri(musica.thumbnailUrl?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(musica.id)
            .setUri(Uri.fromFile(File(musica.caminhoArquivo)))
            .setMediaMetadata(metadata)
            .build()
    }

    fun paraListaMediaItems(musicas: List<Musica>): List<MediaItem> =
        musicas.map { paraMediaItem(it) }
}
