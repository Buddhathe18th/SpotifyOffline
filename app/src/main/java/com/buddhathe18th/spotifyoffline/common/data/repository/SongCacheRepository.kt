package com.buddhathe18th.spotifyoffline.common.data.repository

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.database.ArtistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.SongArtistCrossRef
import com.buddhathe18th.spotifyoffline.common.data.database.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongCacheRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val songDao = db.songDao()
    private val artistDao = db.artistDao()
    private val songArtistDao = db.songArtistDao()

    suspend fun syncFromMediaStore(context: Context) =
            withContext(Dispatchers.IO) {
                val projection =
                        arrayOf(
                                MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.DATE_ADDED
                        )

                val songs = mutableListOf<SongEntity>()
                val artists = mutableSetOf<ArtistEntity>()
                val songArtistLinks = mutableListOf<SongArtistCrossRef>()

                context.contentResolver.query(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                projection,
                                null,
                                null,
                                "${MediaStore.Audio.Media.TITLE} ASC"
                        )
                        ?.use { cursor ->
                            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                            val titleCol =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                            val artistCol =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                            val albumCol =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                            val durationCol =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                            val dateCol =
                                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                            while (cursor.moveToNext()) {
                                val songId = cursor.getLong(idCol).toString()
                                val title = cursor.getString(titleCol) ?: "Unknown"
                                val artistString = cursor.getString(artistCol) ?: "Unknown Artist"

                                // Add song
                                songs.add(
                                        SongEntity(
                                                id = songId,
                                                title = title,
                                                album = cursor.getString(albumCol),
                                                durationMs = cursor.getLong(durationCol),
                                                dateAdded = cursor.getLong(dateCol),
                                                titleNormalized = title.lowercase()
                                        )
                                )

                                // Parse and add artists
                                val artistNames = parseArtists(artistString)
                                artistNames.forEachIndexed { index, artistName ->
                                    val artistId = generateArtistId(artistName)

                                    artists.add(
                                            ArtistEntity(
                                                    id = artistId,
                                                    name = artistName,
                                                    nameNormalized = artistName.lowercase()
                                            )
                                    )

                                    songArtistLinks.add(
                                            SongArtistCrossRef(
                                                    songId = songId,
                                                    artistId = artistId,
                                                    order = index
                                            )
                                    )
                                }
                            }
                        }

                // Clear and rebuild (simple strategy for now)
                Log.d("Sync", "Clearing old data...")
                songDao.deleteAll()
                songArtistDao.deleteAll()

                // Insert in correct order (artists first, then songs, then links)
                Log.d("Sync", "Inserting ${artists.size} artists...")
                artistDao.insertArtists(artists.toList())

                Log.d("Sync", "Inserting ${songs.size} songs...")
                songDao.insertSongs(songs)

                Log.d("Sync", "Creating ${songArtistLinks.size} song-artist links...")
                songArtistDao.insertAll(songArtistLinks)

                Log.d("Sync", "Sync complete!")
            }

    private fun parseArtists(artistString: String): List<String> {
        return artistString
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != "Unknown Artist" && it != "<unknown>" }
                .ifEmpty { listOf("Unknown Artist") }
    }

    private fun generateArtistId(artistName: String): String {
        return artistName.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
    }
}
