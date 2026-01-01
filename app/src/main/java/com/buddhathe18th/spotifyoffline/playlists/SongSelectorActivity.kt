package com.buddhathe18th.spotifyoffline.playlists

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.main.SongWithArtistsAdapter
import kotlinx.coroutines.launch

class SongSelectorActivity : AppCompatActivity() {

    private val selectedSongIds = mutableSetOf<String>()
    private lateinit var adapter: SongWithArtistsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_selector)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAllSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SongWithArtistsAdapter(emptyList()) { song ->
            // Toggle selection
            if (selectedSongIds.contains(song.song.id)) {
                selectedSongIds.remove(song.song.id)
            } else {
                selectedSongIds.add(song.song.id)
            }
        }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.buttonDone).setOnClickListener {
            val intent = Intent()
            intent.putStringArrayListExtra("SELECTED_SONG_IDS", ArrayList(selectedSongIds))
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        loadAllSongs()
    }

    private fun loadAllSongs() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@SongSelectorActivity)
            db.songDao().getAllSongsWithArtists().collect { songs ->
                adapter.updateSongs(songs)
            }
        }
    }
}
