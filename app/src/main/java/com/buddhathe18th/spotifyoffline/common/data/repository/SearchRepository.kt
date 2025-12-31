package com.buddhathe18th.spotifyoffline.common.data.repository

import android.content.Context
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.database.ArtistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists
import kotlinx.coroutines.flow.Flow

class SearchRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)

    fun searchSongs(query: String): Flow<List<SongWithArtists>> {
        return db.songDao().searchSongsWithArtists(query.lowercase())
    }

    fun searchArtists(query: String): Flow<List<ArtistEntity>> {
        return db.artistDao().searchArtistsByName(query.lowercase())
    }
}
