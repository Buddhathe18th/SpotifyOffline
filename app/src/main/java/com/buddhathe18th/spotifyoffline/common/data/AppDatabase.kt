package com.buddhathe18th.spotifyoffline.common.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.buddhathe18th.spotifyoffline.common.data.dao.ArtistDao
import com.buddhathe18th.spotifyoffline.common.data.dao.PlaylistDao
import com.buddhathe18th.spotifyoffline.common.data.dao.PlaylistSongDao
import com.buddhathe18th.spotifyoffline.common.data.dao.SongArtistDao
import com.buddhathe18th.spotifyoffline.common.data.dao.SongDao
import com.buddhathe18th.spotifyoffline.common.data.database.ArtistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistEntity
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistSongCrossRef
import com.buddhathe18th.spotifyoffline.common.data.database.SongArtistCrossRef
import com.buddhathe18th.spotifyoffline.common.data.database.SongEntity

@Database(
        entities =
                [
                        SongEntity::class,
                        ArtistEntity::class,
                        SongArtistCrossRef::class,
                        PlaylistEntity::class,
                        PlaylistSongCrossRef::class],
        version = 3,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun artistDao(): ArtistDao
    abstract fun songArtistDao(): SongArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                AppDatabase::class.java,
                                                "music_database"
                                        )
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}
