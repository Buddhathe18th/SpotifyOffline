package com.buddhathe18th.spotifyoffline.common.data.dao

import androidx.room.*
import com.buddhathe18th.spotifyoffline.common.data.database.SongArtistCrossRef

@Dao
interface SongArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<SongArtistCrossRef>)
    
    @Query("DELETE FROM song_artists WHERE songId = :songId")
    suspend fun deleteArtistsForSong(songId: String)
    
    @Query("DELETE FROM song_artists")
    suspend fun deleteAll()
}
