package com.buddhathe18th.spotifyoffline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [Index(value = ["name_normalized"])]
)
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "name_normalized") 
    val nameNormalized: String
)
