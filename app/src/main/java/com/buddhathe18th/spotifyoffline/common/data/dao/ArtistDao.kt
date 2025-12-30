package com.buddhathe18th.spotifyoffline.common.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.buddhathe18th.spotifyoffline.common.data.database.ArtistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.ArtistWithSongs

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name")
    fun getAllArtists(): Flow<List<ArtistEntity>>
    
    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getArtistWithSongs(artistId: String): Flow<ArtistWithSongs>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)
    
    @Query("DELETE FROM artists")
    suspend fun deleteAll()
}
