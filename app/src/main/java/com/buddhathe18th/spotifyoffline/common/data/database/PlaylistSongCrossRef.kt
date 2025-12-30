package com.buddhathe18th.spotifyoffline.common.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.buddhathe18th.spotifyoffline.common.data.database.SongEntity
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistEntity

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songId"), Index("playlistId")]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val position: Int,
    val addedAt: Long
)
