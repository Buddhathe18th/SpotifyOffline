package com.buddhathe18th.spotifyoffline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val musicPlayer = MusicPlayer()
    private val handler = Handler(Looper.getMainLooper())
    private var isUpdatingProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout FIRST
        setContentView(R.layout.activity_main)

        val buttonPlayPause = findViewById<ImageButton>(R.id.buttonPlayPause)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        var adapter =
                SongAdapter(emptyList()) { song -> Log.d("MainActivity", "Clicked: ${song.title}") }
        val scanButton = findViewById<Button>(R.id.buttonScan)

        buttonPlayPause.isEnabled = false

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonPlayPause.setOnClickListener {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause()
                buttonPlayPause.setImageResource(android.R.drawable.ic_media_play)
                stopProgressUpdates()

                Log.d("MainActivity", "Paused playback")
            } else {
                musicPlayer.resume()
                buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                startProgressUpdates()
                Log.d("MainActivity", "Resumed playback")
            }
        }

        scanButton.setOnClickListener {
            Log.d("MainActivity", "Scan button clicked")

            MediaStoreSongRepository.scanSpotifyOfflineFolder(this) {
                lifecycleScope.launch {
                    val songs =
                            withContext(Dispatchers.IO) {
                                MediaStoreSongRepository.loadSongs(this@MainActivity)
                            }
                    Log.d("MainActivity", "Reloaded ${songs.size} songs after scan")
                    setupRecyclerView(songs)
                }
            }
        }

        // Now permission callback can safely access views
        ensureAudioPermission {
            Log.d("MainActivity", "Permission granted")
            lifecycleScope.launch {
                val songs =
                        withContext(Dispatchers.IO) {
                            MediaStoreSongRepository.loadSongs(this@MainActivity)
                        }
                Log.d("MainActivity", "Loaded ${songs.size} songs")
                setupRecyclerView(songs)
            }
            // Now the RecyclerView exists, so this works

        }
    }

    // Pick correct permission for the SDK version
    private val audioPermission: String
        get() =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO // Android 13+
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE // Older devices
                }

    private var pendingOnGranted: (() -> Unit)? = null

    // Launcher that shows the permission dialog and gets the result
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

    // Helper that ensures permission, then runs the callback
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

    private fun setupRecyclerView(songs: List<Song>) {
        Log.d("MainActivity", "setupRecyclerView called with ${songs.size} songs")
        songs.forEach { Log.d("MainActivity", "  - Title: ${it.title}, Artist: ${it.artist}") }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        Log.d("MainActivity", "RecyclerView is null: ${recyclerView == null}")

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter =
                SongAdapter(songs) { song ->
                    Log.d("MainActivity", "Clicked: ${song.title}")

                    val textNowPlaying = findViewById<TextView>(R.id.textNowPlaying)
                    val buttonPlayPause = findViewById<ImageButton>(R.id.buttonPlayPause)

                    musicPlayer.play(
                            context = this,
                            uri = song.uri,
                            onPrepared = {
                                Log.d("MainActivity", "Playback started for ${song.title}")
                                runOnUiThread {
                                    textNowPlaying.text = song.title
                                    buttonPlayPause.setImageResource(
                                            android.R.drawable.ic_media_pause
                                    )
                                    buttonPlayPause.isEnabled = true
                                    startProgressUpdates() // Start updating progress
                                }
                            },
                            onCompletion = {
                                Log.d("MainActivity", "Playback completed for ${song.title}")
                                runOnUiThread {
                                    textNowPlaying.text = "Nothing playing"
                                    buttonPlayPause.setImageResource(
                                            android.R.drawable.ic_media_play
                                    )
                                    stopProgressUpdates() // Stop updating progress
                                }
                            }
                    )
                }

        recyclerView.adapter = adapter
        Log.d("MainActivity", "Adapter set! Item count: ${adapter.itemCount}")
    }

    // If needed, convert this into a foreground service for more reliable playback
    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.stopAndRelease()
        stopProgressUpdates()
        Log.d("MainActivity", "Activity destroyed, released player")
    }

    private val updateProgressRunnable =
            object : Runnable {
                override fun run() {
                    if (musicPlayer.isPlaying()) {
                        val currentPos = musicPlayer.getCurrentPosition()
                        val duration = musicPlayer.getDuration()

                        if (duration > 0) {
                            val seekBar = findViewById<SeekBar>(R.id.seekBarProgress)
                            val textCurrent = findViewById<TextView>(R.id.textCurrentTime)
                            val textTotal = findViewById<TextView>(R.id.textTotalTime)

                            seekBar.max = duration
                            seekBar.progress = currentPos

                            textCurrent.text = formatTime(currentPos)
                            textTotal.text = formatTime(duration)
                        }
                    }

                    if (isUpdatingProgress) {
                        handler.postDelayed(this, 1000) // Update every second
                    }
                }
            }

    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun startProgressUpdates() {
        isUpdatingProgress = true
        handler.post(updateProgressRunnable)
    }

    private fun stopProgressUpdates() {
        isUpdatingProgress = false
        handler.removeCallbacks(updateProgressRunnable)
    }
}
