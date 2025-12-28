package com.buddhathe18th.spotifyoffline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index(value = ["title_normalized"])]
)
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val album: String?,
    val durationMs: Long,
    val dateAdded: Long,
    @ColumnInfo(name = "title_normalized") 
    val titleNormalized: String
)
