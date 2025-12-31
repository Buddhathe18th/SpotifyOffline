package com.buddhathe18th.spotifyoffline.playlists

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.database.SongEntity
import com.buddhathe18th.spotifyoffline.main.SongWithArtistsAdapter
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.BaseActivity

class PlaylistDetailActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongWithArtistsAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        val playlistId = intent.getStringExtra("PLAYLIST_ID") ?: return finish()
        val playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: "Playlist"

        findViewById<TextView>(R.id.textPlaylistName).text = playlistName
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SongWithArtistsAdapter(emptyList()) { song ->
            // Play song
        }
        recyclerView.adapter = adapter

        loadPlaylistSongs(playlistId)
    }

    private fun loadPlaylistSongs(playlistId: String) {
        lifecycleScope.launch {
            db.playlistDao().getPlaylistWithSongs(playlistId).collect { playlistWithSongs ->
                val songIds = playlistWithSongs.songs.map { it.id }
                
                db.songDao().getAllSongsWithArtists().collect { allSongs ->
                    val songs = allSongs.filter { it.song.id in songIds }
                    adapter.updateSongs(songs)
                    
                    findViewById<TextView>(R.id.textPlaylistInfo).text = 
                        "${songs.size} songs"
                }
            }
        }
    }
}
