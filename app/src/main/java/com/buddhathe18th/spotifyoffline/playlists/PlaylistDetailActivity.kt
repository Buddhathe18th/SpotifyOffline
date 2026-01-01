package com.buddhathe18th.spotifyoffline.playlists

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.BaseActivity
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaylistDetailActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistSongAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val playlistRepo by lazy { PlaylistRepository(this) }
    private lateinit var playlistId: String

    private val addSongsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val songIds = result.data?.getStringArrayListExtra("SELECTED_SONG_IDS")

                    var added = 0

                    if (songIds != null && songIds.isNotEmpty()) {
                        songIds.forEach { songId ->
                            Log.d(
                                    "PlaylistDetailActivity",
                                    "Adding songId: $songId to playlistId: $playlistId"
                            )

                            lifecycleScope.launch {
                                val value =
                                        playlistRepo.checkSongInPlaylist(playlistId, songId).first()
                                if (value == null || value == 0) {
                                    added++
                                    playlistRepo.addSongToPlaylist(playlistId, songId)
                                    Log.d("PlaylistDetailActivity", "Added song $songId")
                                    Log.d(
                                            "PlaylistDetailActivity",
                                            "Total songs added so far: $added"
                                    )
                                } else {
                                    Log.d(
                                            "PlaylistDetailActivity",
                                            "Song $songId already exists, skipping"
                                    )
                                }
                            }
                        }

                        Log.d("PlaylistDetailActivity", "Total songs added so far: $added")
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        playlistId = intent.getStringExtra("PLAYLIST_ID") ?: return finish()
        val playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: "Playlist"

        findViewById<TextView>(R.id.textPlaylistName).text = playlistName
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.buttonPlayAll).setOnClickListener {
            Log.d("PlaylistDetailActivity", "Play All button clicked")
            lifecycleScope.launch {
                val songs = adapter.getCurrentSongs()

                if (songs.isNotEmpty()) {
                    playQueue.setQueue(songs, 0)
                    playSongAtCurrentIndex()
                    Log.d(
                            "PlaylistDetailActivity",
                            "Added ${songs.size} songs to play queue and started playback."
                    )
                } else {
                    Log.d("PlaylistDetailActivity", "No songs to play in this playlist.")
                }
                updatePlayerUI()
            }
        }

        findViewById<Button>(R.id.buttonAddSongToPlaylist).setOnClickListener {
            val intent = Intent(this, SongSelectorActivity::class.java)
            intent.putExtra("PLAYLIST_ID", playlistId)
            addSongsLauncher.launch(intent)
        }

        recyclerView = findViewById(R.id.recyclerSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Use new adapter with remove callback
        adapter =
                PlaylistSongAdapter(
                        emptyList(),
                        onClick = { song ->
                            // Play song
                        },
                        onRemove = { song ->
                            // Remove song from playlist
                            lifecycleScope.launch {
                                playlistRepo.removeSongFromPlaylist(playlistId, song.song.id)

                                android.widget.Toast.makeText(
                                                this@PlaylistDetailActivity,
                                                "Removed ${song.song.title} from playlist",
                                                android.widget.Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        }
                )
        recyclerView.adapter = adapter

        loadPlaylistSongs(playlistId)
    }

    private fun loadPlaylistSongs(playlistId: String) {
        lifecycleScope.launch {
            db.playlistDao().getPlaylistSongsInOrder(playlistId).collect { songs ->
                adapter.updateSongs(songs)
                findViewById<TextView>(R.id.textPlaylistInfo).text = "${songs.size} songs"
            }
        }
    }
}
