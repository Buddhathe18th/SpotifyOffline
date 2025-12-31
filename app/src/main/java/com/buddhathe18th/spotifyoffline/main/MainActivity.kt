package com.buddhathe18th.spotifyoffline.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.BaseActivity
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.common.data.repository.PlaylistRepository
import com.buddhathe18th.spotifyoffline.common.data.repository.SongCacheRepository
import com.buddhathe18th.spotifyoffline.playlists.PlaylistActivity
import com.buddhathe18th.spotifyoffline.queue.QueueActivity
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var songAdapter: SongWithArtistsAdapter

    private val queueLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val playCurrent =
                            result.data?.getBooleanExtra(QueueActivity.EXTRA_PLAY_CURRENT, false)
                                    ?: false

                    if (playCurrent) {
                        Log.d("MainActivity", "Playing song at current index after removal")
                        if (playQueue.getCurrentSong() != null) {
                            playSongAtCurrentIndex()
                        } else {
                            Log.d("MainActivity", "Queue empty after removal, stopping playback")
                            musicPlayer.stopAndRelease()
                            updatePlayerUI()
                            stopProgressUpdates()
                        }
                        return@registerForActivityResult
                    }

                    val jumpIndex =
                            result.data?.getIntExtra(QueueActivity.EXTRA_JUMP_INDEX, -1) ?: -1
                    if (jumpIndex >= 0) {
                        Log.d(
                                "MainActivity",
                                "Jumping to index from queue: $jumpIndex to song ${playQueue.getQueue()[jumpIndex].song.title}"
                        )
                        playQueue.setCurrentIndex(jumpIndex)
                        playSongAtCurrentIndex()
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        val scanButton = findViewById<Button>(R.id.buttonScan)
        val buttonViewPlaylists = findViewById<Button>(R.id.buttonViewPlaylists)

        // Initialize adapter with empty list and click handler
        songAdapter =
                SongWithArtistsAdapter(emptyList()) { songWithArtists ->
                    Log.d("MainActivity", "Clicked: ${songWithArtists.song.title}")

                    val currentQueue = playQueue.getQueue()
                    if (songWithArtists == playQueue.getCurrentSong()) {
                        Log.d(
                                "MainActivity",
                                "Song: ${songWithArtists.song.title} is already playing"
                        )
                        musicPlayer.restartCurrentSong()
                    } else if (currentQueue.contains(songWithArtists)) {
                        Log.d(
                                "MainActivity",
                                "Song: ${songWithArtists.song.title} is already in queue"
                        )
                        val indexInQueue = currentQueue.indexOf(songWithArtists)
                        playQueue.setCurrentIndex(indexInQueue)
                    } else {
                        Log.d(
                                "MainActivity",
                                "Song: ${songWithArtists.song.title} is a new song in queue"
                        )

                        if (playQueue.size() == 0) {
                            playQueue.addToQueue(0, songWithArtists)
                            playQueue.setQueue(playQueue.getQueue(), 0)
                        } else {
                            playQueue.addToQueue(playQueue.getCurrentIndex() + 1, songWithArtists)
                            playQueue.setQueue(
                                    playQueue.getQueue(),
                                    playQueue.getCurrentIndex() + 1
                            )
                        }
                    }
                    playSongAtCurrentIndex()
                }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = songAdapter

        // buttonViewQueue is now in BaseActivity
        buttonViewQueue.setOnClickListener {
            queueLauncher.launch(Intent(this, QueueActivity::class.java))
        }

        scanButton.setOnClickListener {
            Log.d("MainActivity", "Scan button clicked")

            lifecycleScope.launch {
                val repository = SongCacheRepository(this@MainActivity)
                try {
                    repository.syncFromMediaStore(this@MainActivity)
                    Log.d("MainActivity", "Scan complete!")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Scan failed", e)
                }
            }
        }

        buttonViewPlaylists.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }

        ensureAudioPermission {
            Log.d("MainActivity", "Permission granted, loading songs from database...")

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(this@MainActivity)
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        db.songDao().getAllSongsWithArtists().collect { songs ->
                            Log.d("MainActivity", "Loaded ${songs.size} songs from database")
                            songAdapter.updateSongs(songs)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error loading songs from database", e)
                }
            }
        }

        playlistTest()
    }

    private val audioPermission: String
        get() =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

    private var pendingOnGranted: (() -> Unit)? = null

    private val audioPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Log.d("MainActivity", "Permission granted")
                    pendingOnGranted?.invoke()
                } else {
                    Log.d("MainActivity", "Permission denied")
                }
                pendingOnGranted = null
            }

    fun ensureAudioPermission(onGranted: () -> Unit) {
        val granted =
                ContextCompat.checkSelfPermission(this, audioPermission) ==
                        PackageManager.PERMISSION_GRANTED

        if (granted) {
            onGranted()
        } else {
            pendingOnGranted = onGranted
            audioPermissionLauncher.launch(audioPermission)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stopAndRelease()
        Log.d("MainActivity", "Activity destroyed, released player")
    }

    override fun playSongAtCurrentIndex() {
        val song = playQueue.getCurrentSong() ?: return

        musicPlayer.play(
                context = this,
                uri = Uri.parse(song.song.uri),
                onPrepared = {
                    Log.d("MainActivity", "Playback started for ${song.song.title}")
                    runOnUiThread {
                        updatePlayerUI()
                        startProgressUpdates()
                    }
                },
                onCompletion = {
                    Log.d("MainActivity", "Playback completed for ${song.song.title}")
                    runOnUiThread {
                        if (playQueue.hasNext()) {
                            playNextSong()
                        } else {
                            updatePlayerUI()
                            stopProgressUpdates()
                        }
                    }
                }
        )
    }

    private fun playlistTest() {
        lifecycleScope.launch {
            val repo = PlaylistRepository(this@MainActivity)

            repo.getAllPlaylists().collect { playlists ->
                if (playlists.isEmpty()) {
                    val playlist = repo.createPlaylist("My Favorites")

                    val db = AppDatabase.getDatabase(this@MainActivity)
                    db.songDao().getAllSongsWithArtists().collect { songs ->
                        songs.take(3).forEach { song ->
                            repo.addSongToPlaylist(playlist.id, song.song.id)
                        }
                    }

                    Log.d("MainActivity", "Created test playlist")
                }
                val playlist = repo.createPlaylist("aaaaa")
                val db = AppDatabase.getDatabase(this@MainActivity)
                db.songDao().getAllSongsWithArtists().collect { songs ->
                    songs.take(30).forEach { song ->
                        repo.addSongToPlaylist(playlist.id, song.song.id)
                    }
                }
            }
        }
    }
}
