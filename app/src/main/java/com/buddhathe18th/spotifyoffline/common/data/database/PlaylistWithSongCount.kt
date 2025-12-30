package com.buddhathe18th.spotifyoffline.common.data.database

data class PlaylistWithSongCount(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val songCount: Int
)
