package com.buddhathe18th.spotifyoffline.common.data.dao

import androidx.room.*
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: PlaylistSongCrossRef)
    
    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: String): Int?
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    
    @Query("""
        UPDATE playlist_songs 
        SET position = position - 1 
        WHERE playlistId = :playlistId AND position > :removedPosition
    """)
    suspend fun reorderAfterRemoval(playlistId: String, removedPosition: Int)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: String)

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    fun isSongInPlaylist(playlistId: String, songId: String): Flow<Int?>
}
