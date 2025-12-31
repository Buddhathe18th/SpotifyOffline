package com.buddhathe18th.spotifyoffline.common.data.repository

import android.content.Context
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistSongCrossRef
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistWithSongCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaylistRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val playlistDao = db.playlistDao()
    private val playlistSongDao = db.playlistSongDao()

    fun getAllPlaylists(): Flow<List<PlaylistWithSongCount>> {
        return playlistDao.getAllPlaylistsWithCount()
    }

    suspend fun createPlaylist(name: String): PlaylistEntity =
            withContext(Dispatchers.IO) {
                val playlist =
                        PlaylistEntity(
                                name = name,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                        )
                playlistDao.insertPlaylist(playlist)
                playlist
            }

    suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val maxPos = playlistSongDao.getMaxPosition(playlistId) ?: -1
                    playlistSongDao.insert(
                            PlaylistSongCrossRef(
                                    playlistId = playlistId,
                                    songId = songId,
                                    position = maxPos + 1,
                                    addedAt = System.currentTimeMillis()
                            )
                    )
                    true
                } catch (e: Exception) {
                    false
                }
            }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    // Find position of song to remove
                    // val songs = playlistDao.getPlaylistSongsInOrder(playlistId)
                    // Remove and reorder
                    playlistSongDao.removeSongFromPlaylist(playlistId, songId)
                    true
                } catch (e: Exception) {
                    false
                }
            }

    suspend fun deletePlaylist(playlistId: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val playlist = PlaylistEntity(playlistId, "", 0, 0)
                    playlistDao.deletePlaylist(playlist)
                    true
                } catch (e: Exception) {
                    false
                }
            }
}
