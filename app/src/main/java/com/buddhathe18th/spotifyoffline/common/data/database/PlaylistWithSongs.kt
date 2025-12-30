package com.buddhathe18th.spotifyoffline.common.data.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistSongCrossRef

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongEntity>
)
