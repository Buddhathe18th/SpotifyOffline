package com.buddhathe18th.spotifyoffline

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.buddhathe18th.spotifyoffline.common.data.database.SongEntity
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists

@Dao
interface SongDao {
    @Transaction
    @Query("SELECT * FROM songs ORDER BY title")
    fun getAllSongsWithArtists(): Flow<List<SongWithArtists>>
    
    @Transaction
    @Query("""
        SELECT DISTINCT songs.* FROM songs
        LEFT JOIN song_artists ON songs.id = song_artists.songId
        LEFT JOIN artists ON song_artists.artistId = artists.id
        WHERE songs.title_normalized LIKE '%' || :query || '%'
        OR artists.name_normalized LIKE '%' || :query || '%'
    """)
    fun searchSongsWithArtists(query: String): Flow<List<SongWithArtists>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAll()
}
