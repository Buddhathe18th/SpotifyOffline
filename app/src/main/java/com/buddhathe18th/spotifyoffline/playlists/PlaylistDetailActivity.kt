package com.buddhathe18th.spotifyoffline.playlists

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.BaseActivity
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.main.SongWithArtistsAdapter
import kotlinx.coroutines.launch
import android.util.Log

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
        findViewById<Button>(R.id.buttonPlayAll).setOnClickListener {
            findViewById<Button>(R.id.buttonPlayAll).setOnClickListener {
                Log.d("PlaylistDetailActivity", "Play All button clicked")
                lifecycleScope.launch {
                    // Get all songs from the current adapter
                    val songs = adapter.getCurrentSongs() // You'll need to expose this from adapter

                    if (songs.isNotEmpty()) {
                        playQueue.setQueue(songs, 0)
                        playSongAtCurrentIndex()
                        Log.d("PlaylistDetailActivity", "Added ${songs.size} songs to play queue and started playback.")
                    }else{
                        Log.d("PlaylistDetailActivity", "No songs to play in this playlist.")
                    }

                }
                updatePlayerUI()
            }
        }

        recyclerView = findViewById(R.id.recyclerSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter =
                SongWithArtistsAdapter(emptyList()) { song ->
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

                    findViewById<TextView>(R.id.textPlaylistInfo).text = "${songs.size} songs"
                }
            }
        }
    }
}
