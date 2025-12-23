package com.buddhathe18th.spotifyoffline

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import android.util.Log

object MediaStoreSongRepository {

    fun loadSongs(context: Context): List<Song> {
        val resolver = context.contentResolver

        // Use external audio collection
        val collection: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // Columns: _ID, TITLE, ARTIST, DURATION, DATA (for path filter)
        val projection = arrayOf(
            BaseColumns._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA // deprecated but fine for this path filter task
        )

        // Only music files, and only those whose path contains "/Music/SpotifyOffline/"
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
        "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Music/SpotifyOffline/%")



        val songs = mutableListOf<Song>()

        resolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(BaseColumns._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Unknown title"
                val artist = cursor.getString(artistCol) ?: "Unknown artist"
                val durationMs = cursor.getLong(durationCol)

                // Build content://media/external/audio/media/<id>
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                songs += Song(
                    title = title,
                    artist = artist,
                    uri = contentUri,
                    durationMs = durationMs
                )
            }
        }

        return songs
    }

    fun scanSpotifyOfflineFolder(context: Context, onComplete: () -> Unit) {
        // Path to the folder
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "SpotifyOffline"
        )

        if (!musicDir.exists()) {
            Log.d("MediaStoreSongRepository", "SpotifyOffline folder does not exist")
            onComplete()
            return
        }

        // Get all audio files in that folder
        val audioFiles = musicDir.listFiles { file ->
            file.isFile && file.extension in listOf("mp3", "m4a", "flac", "wav", "ogg")
        }

        if (audioFiles.isNullOrEmpty()) {
            Log.d("MediaStoreSongRepository", "No audio files found to scan")
            onComplete()
            return
        }

        val paths = audioFiles.map { it.absolutePath }.toTypedArray()

        Log.d("MediaStoreSongRepository", "Scanning ${paths.size} files...")

        MediaScannerConnection.scanFile(
            context,
            paths,
            null
        ) { _, _ ->
            // Called once per file; when all are done, callback is complete
            Log.d("MediaStoreSongRepository", "Scan complete")
            onComplete()
        }
    }
}
