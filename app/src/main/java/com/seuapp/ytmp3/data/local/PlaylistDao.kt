package com.seuapp.ytmp3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY nome ASC")
    fun listarTodas(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE nome LIKE '%' || :termo || '%' ORDER BY nome ASC")
    fun buscar(termo: String): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(playlist: Playlist): Long

    @Delete
    suspend fun deletar(playlist: Playlist)

    @Query("SELECT * FROM musicas INNER JOIN playlist_musica ON musicas.id = playlist_musica.musicaId WHERE playlist_musica.playlistId = :playlistId")
    fun listarMusicasDaPlaylist(playlistId: Long): Flow<List<Musica>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun adicionarMusica(playlistMusica: PlaylistMusica)

    @Query("DELETE FROM playlist_musica WHERE playlistId = :playlistId AND musicaId = :musicaId")
    suspend fun removerMusica(playlistId: Long, musicaId: String)
}
