package com.buddhathe18th.spotifyoffline.playlists

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistWithSongCount
import com.buddhathe18th.spotifyoffline.common.data.repository.PlaylistRepository
import com.buddhathe18th.spotifyoffline.common.BaseActivity
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager

class PlaylistActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private lateinit var emptyState: LinearLayout
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { PlaylistRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        recyclerView = findViewById(R.id.recyclerPlaylists)
        emptyState = findViewById(R.id.layoutEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup adapter
        adapter = PlaylistAdapter(emptyList()) { playlist -> openPlaylistDetails(playlist) }
        recyclerView.adapter = adapter

        // Back button
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener { finish() }

        // Add playlist buttons
        findViewById<ImageButton>(R.id.buttonAddPlaylist).setOnClickListener {
            showCreatePlaylistDialog()
        }
        findViewById<Button>(R.id.buttonCreateFirst).setOnClickListener {
            showCreatePlaylistDialog()
        }

        // Load playlists
        loadPlaylists()
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            repository.getAllPlaylists().collect { playlists ->
                if (playlists.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updatePlaylists(playlists)
                }
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val input =
                EditText(this).apply {
                    hint = "Playlist name"
                    setPadding(50, 40, 50, 40)
                }

        AlertDialog.Builder(this)
                .setTitle("Create Playlist")
                .setView(input)
                .setPositiveButton("Create") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) {
                        createPlaylist(name)
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun createPlaylist(name: String) {
        lifecycleScope.launch {
            repository.createPlaylist(name)
            Toast.makeText(this@PlaylistActivity, "Created: $name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlaylistDetails(playlist: PlaylistWithSongCount) {
        val intent = Intent(this, PlaylistDetailActivity::class.java)
        intent.putExtra("PLAYLIST_ID", playlist.id)
        intent.putExtra("PLAYLIST_NAME", playlist.name)
        startActivity(intent)
    }
}
