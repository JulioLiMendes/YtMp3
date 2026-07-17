package com.seuapp.ytmp3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicaDao {

    @Query("SELECT * FROM musicas ORDER BY dataAdicionado DESC")
    fun listarTodas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE favorito = 1 ORDER BY dataAdicionado DESC")
    fun listarFavoritas(): Flow<List<Musica>>

    @Query("SELECT * FROM musicas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): Musica?

    @Query("SELECT * FROM musicas WHERE titulo LIKE '%' || :termo || '%' OR artista LIKE '%' || :termo || '%'")
    fun buscar(termo: String): Flow<List<Musica>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(musica: Musica)

    @Update
    suspend fun atualizar(musica: Musica)

    @Query("UPDATE musicas SET favorito = :favorito WHERE id = :id")
    suspend fun definirFavorito(id: String, favorito: Boolean)

    @Delete
    suspend fun deletar(musica: Musica)

    @Query("DELETE FROM musicas WHERE id = :id")
    suspend fun deletarPorId(id: String)
}
