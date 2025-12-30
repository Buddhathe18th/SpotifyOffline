package com.buddhathe18th.spotifyoffline.common.data.dao

import androidx.room.*
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistWithSongCount
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistWithSongs
import com.buddhathe18th.spotifyoffline.common.data.database.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query(
            """
        SELECT p.id, p.name, p.createdAt, p.updatedAt, COUNT(ps.songId) as songCount
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlistId
        GROUP BY p.id
        ORDER BY p.updatedAt DESC
    """
    )
    fun getAllPlaylistsWithCount(): Flow<List<PlaylistWithSongCount>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: String): Flow<PlaylistWithSongs>

    @Transaction
    @Query(
            """
    SELECT s.* FROM songs s
    INNER JOIN playlist_songs ps ON s.id = ps.songId
    WHERE ps.playlistId = :playlistId
    ORDER BY ps.position ASC
    """
    )
    fun getPlaylistSongsInOrder(playlistId: String): Flow<List<SongEntity>>

    @Insert suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Delete suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :newName, updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: String, newName: String, timestamp: Long)
}
