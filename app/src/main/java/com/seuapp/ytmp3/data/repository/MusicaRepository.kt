package com.seuapp.ytmp3.data.repository

import android.content.Context
import android.os.Environment
import com.seuapp.ytmp3.data.local.AppDatabase
import com.seuapp.ytmp3.data.local.Musica
import com.seuapp.ytmp3.data.local.Playlist
import com.seuapp.ytmp3.data.local.PlaylistMusica
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Ponto único de acesso aos dados de músicas.
 * Também é responsável por garantir que a pasta própria do app exista
 * (Android/data/com.seuapp.ytmp3/files/Music/YtMp3), organizando os
 * arquivos baixados fora do restante do sistema de arquivos do usuário.
 */
class MusicaRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao = database.musicaDao()
    private val playlistDao = database.playlistDao()

    fun listarTodas(): Flow<List<Musica>> = dao.listarTodas()

    fun listarFavoritas(): Flow<List<Musica>> = dao.listarFavoritas()

    fun buscar(termo: String): Flow<List<Musica>> = dao.buscar(termo)

    suspend fun buscarPorId(id: String): Musica? = dao.buscarPorId(id)

    suspend fun salvar(musica: Musica) = dao.inserir(musica)

    suspend fun atualizar(musica: Musica) = dao.atualizar(musica)

    suspend fun alternarFavorito(musica: Musica) {
        dao.definirFavorito(musica.id, !musica.favorito)
    }

    suspend fun definirFavorito(id: String, favorito: Boolean) {
        dao.definirFavorito(id, favorito)
    }

    suspend fun remover(musica: Musica) {
        // remove o registro do banco...
        dao.deletar(musica)
        // ...e também o arquivo físico, para não deixar lixo na pasta do app
        val arquivo = File(musica.caminhoArquivo)
        if (arquivo.exists()) {
            arquivo.delete()
        }
    }

    // Playlists
    fun listarPlaylists(): Flow<List<Playlist>> = playlistDao.listarTodas()

    fun buscarPlaylists(termo: String): Flow<List<Playlist>> = playlistDao.buscar(termo)

    fun listarMusicasDaPlaylist(playlistId: Long): Flow<List<Musica>> =
        playlistDao.listarMusicasDaPlaylist(playlistId)

    suspend fun criarPlaylist(nome: String): Long =
        playlistDao.inserir(Playlist(nome = nome))

    suspend fun deletarPlaylist(playlist: Playlist) =
        playlistDao.deletar(playlist)

    suspend fun adicionarMusicaNaPlaylist(playlistId: Long, musicaId: String) =
        playlistDao.adicionarMusica(PlaylistMusica(playlistId, musicaId))

    suspend fun removerMusicaDaPlaylist(playlistId: Long, musicaId: String) =
        playlistDao.removerMusica(playlistId, musicaId)

    /**
     * Retorna (criando se necessário) a pasta exclusiva do app onde os
     * mp3s são salvos: /Android/data/com.seuapp.ytmp3/files/Music/YtMp3
     *
     * Essa pasta não exige permissão de armazenamento no Android 10+
     * porque fica dentro do escopo do próprio app.
     */
    fun obterPastaMusicas(): File {
        val pastaBase = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val pastaApp = File(pastaBase, "YtMp3")
        if (!pastaApp.exists()) {
            pastaApp.mkdirs()
        }
        return pastaApp
    }
}
