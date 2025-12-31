package com.buddhathe18th.spotifyoffline.playlists

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.buddhathe18th.spotifyoffline.main.SongWithArtistsAdapter
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistWithSongCount
import com.buddhathe18th.spotifyoffline.R

class PlaylistViewerActivity : ComponentActivity() {

    private lateinit var spinnerPlaylists: Spinner
    private lateinit var textPlaylistInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongWithArtistsAdapter
    
    
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var allPlaylists: List<PlaylistWithSongCount> = emptyList()
    private var currentPlaylistId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_viewer)

        // Initialize views
        spinnerPlaylists = findViewById(R.id.spinnerPlaylists)
        textPlaylistInfo = findViewById(R.id.textPlaylistInfo)
        recyclerView = findViewById(R.id.recyclerPlaylistSongs)

        val buttonBack: Button = findViewById<Button>(R.id.buttonBack)
        val buttonDeletePlaylist: Button = findViewById<Button>(R.id.buttonDeletePlaylist)
        
        // Back button
        buttonBack.setOnClickListener {
            finish()
        }

        buttonDeletePlaylist.setOnClickListener {
            currentPlaylistId?.let { playlistId ->
                lifecycleScope.launch {
                    db.playlistDao().deletePlaylistById(playlistId)
                    Log.d("PlaylistViewer", "Deleted playlist with ID: $playlistId")
                    loadPlaylists()
                }
            }
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SongWithArtistsAdapter(emptyList()) { song ->
            Log.d("PlaylistViewer", "Clicked: ${song.song.title}")
            // TODO: Play song from playlist
        }
        recyclerView.adapter = adapter

        // Load playlists
        loadPlaylists()
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            db.playlistDao().getAllPlaylistsWithCount().collect { playlists ->
                allPlaylists = playlists
                
                if (playlists.isEmpty()) {
                    textPlaylistInfo.text = "No playlists found. Create one first!"
                    spinnerPlaylists.visibility = View.GONE
                } else {
                    spinnerPlaylists.visibility = View.VISIBLE
                    setupSpinner(playlists)
                }
            }
        }
    }

    private fun setupSpinner(playlists: List<PlaylistWithSongCount>) {
        // Create display strings for spinner
        val playlistNames = playlists.map { 
            "${it.name} (${it.songCount} songs)" 
        }

        // Create adapter for spinner
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            playlistNames
        )
        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spinnerPlaylists.adapter = spinnerAdapter

        // Handle selection
        spinnerPlaylists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedPlaylist = playlists[position]
                currentPlaylistId = selectedPlaylist.id
                loadPlaylistSongs(selectedPlaylist.id)
                updatePlaylistInfo(selectedPlaylist)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                textPlaylistInfo.text = "No playlist selected"
                adapter.updateSongs(emptyList())
            }
        }
    }

    private fun loadPlaylistSongs(playlistId: String) {
        lifecycleScope.launch {
            db.playlistDao().getPlaylistWithSongs(playlistId).collect { playlistWithSongs ->
                // Get songs with their artists
                val songIds = playlistWithSongs.songs.map { it.id }
                
                // Fetch complete song info with artists
                db.songDao().getAllSongsWithArtists().collect { allSongsWithArtists ->
                    val playlistSongs = allSongsWithArtists
                        .filter { it.song.id in songIds }
                    
                    adapter.updateSongs(playlistSongs)
                    
                    Log.d("PlaylistViewer", "Loaded ${playlistSongs.size} songs for playlist")
                }
            }
        }
    }

    private fun updatePlaylistInfo(playlist: PlaylistWithSongCount) {
        val createdDate = java.text.SimpleDateFormat(
            "MMM dd, yyyy",
            java.util.Locale.getDefault()
        ).format(java.util.Date(playlist.createdAt))
        
        textPlaylistInfo.text = buildString {
            append("Created: $createdDate\n")
            append("Total songs: ${playlist.songCount}")
        }
    }
}
