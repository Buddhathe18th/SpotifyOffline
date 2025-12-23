package com.buddhathe18th.spotifyoffline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout FIRST
        setContentView(R.layout.activity_main)

        // Now permission callback can safely access views
        ensureAudioPermission {
            Log.d("MainActivity", "Permission granted")

            val songs = MediaStoreSongRepository.loadSongs(this)
            Log.d("MainActivity", "Loaded ${songs.size} songs")

            // Now the RecyclerView exists, so this works
            setupRecyclerView(songs)
        }

        val scanButton = findViewById<Button>(R.id.buttonScan)
        scanButton.setOnClickListener {
            Log.d("MainActivity", "Scan button clicked")

            MediaStoreSongRepository.scanSpotifyOfflineFolder(this) {
                runOnUiThread {
                    val songs = MediaStoreSongRepository.loadSongs(this)
                    Log.d("MainActivity", "Reloaded ${songs.size} songs after scan")

                    setupRecyclerView(songs)
                }
            }
        }
    }

    private fun setupRecyclerView(songs: List<Song>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = SongAdapter(songs) { song -> Log.d("MainActivity", "Clicked: ${song.title}") }

        recyclerView.adapter = adapter
    }
}
