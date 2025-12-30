package com.buddhathe18th.spotifyoffline.common.data.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SongWithArtists(
    @Embedded val song: SongEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SongArtistCrossRef::class,
            parentColumn = "songId",
            entityColumn = "artistId"
        )
    )
    val artists: List<ArtistEntity>
    
) {
    val artistNames: String
        get() = artists.joinToString(", ") { it.name }        
}
