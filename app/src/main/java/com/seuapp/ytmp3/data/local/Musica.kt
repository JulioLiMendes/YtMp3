package com.seuapp.ytmp3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma música baixada e armazenada localmente.
 *
 * @param id normalmente o videoId do YouTube (chave única e estável)
 * @param titulo título extraído do vídeo
 * @param artista canal/uploader do vídeo (pode ser nulo)
 * @param duracaoMs duração em milissegundos, usada pelo player
 * @param caminhoArquivo caminho absoluto do mp3 já convertido, dentro da pasta do app
 * @param thumbnailUrl url da capa/thumbnail (cacheada localmente se possível)
 * @param favorito flag de favorito, alternável pelo usuário
 * @param dataAdicionado timestamp de quando o download foi concluído
 */
@Entity(tableName = "musicas")
data class Musica(
    @PrimaryKey val id: String,
    val titulo: String,
    val artista: String?,
    val duracaoMs: Long,
    val caminhoArquivo: String,
    val thumbnailUrl: String?,
    val favorito: Boolean = false,
    val dataAdicionado: Long = System.currentTimeMillis()
)
