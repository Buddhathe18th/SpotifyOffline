package com.buddhathe18th.spotifyoffline.common.data.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ArtistWithSongs(
    @Embedded val artist: ArtistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SongArtistCrossRef::class,
            parentColumn = "artistId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongEntity>
)
