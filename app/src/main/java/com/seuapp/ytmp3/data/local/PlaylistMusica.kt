package com.seuapp.ytmp3.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_musica",
    primaryKeys = ["playlistId", "musicaId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Musica::class,
            parentColumns = ["id"],
            childColumns = ["musicaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("musicaId")]
)
data class PlaylistMusica(
    val playlistId: Long,
    val musicaId: String
)
