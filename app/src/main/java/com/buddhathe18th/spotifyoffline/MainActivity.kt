package com.buddhathe18th.spotifyoffline

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import android.widget.Button

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

        // Ask for audio/storage permission when the app launches
        ensureAudioPermission {

            // Simple test of the Song model
            val dummy =
                    Song(
                            title = "Test Song",
                            artist = "Test Artist",
                            uri = Uri.parse("content://test"),
                            durationMs = 1000L
                    )
            Log.d("MainActivity", "Permission granted")
            // Test loading songs after permission is granted
            val songs = MediaStoreSongRepository.loadSongs(this)
            Log.d("MainActivity", "Loaded ${songs.size} songs")
            songs.forEach { song -> Log.d("MainActivity", "Song: ${song.title} - ${song.artist}") }
        }

        setContentView(R.layout.activity_main)

        val scanButton = findViewById<Button>(R.id.buttonScan)
        scanButton.setOnClickListener {
            Log.d("MainActivity", "Scan button clicked")

            MediaStoreSongRepository.scanSpotifyOfflineFolder(this) {
                // Rescan complete, reload songs
                val songs = MediaStoreSongRepository.loadSongs(this)
                Log.d("MainActivity", "Reloaded ${songs.size} songs after scan")
                songs.forEach { song ->
                    Log.d("MainActivity", "Song: ${song.title} - ${song.artist}")
                }
            }
        }

        // enableEdgeToEdge()
        // setContent {
        //     SpotifyOfflineTheme {
        //         Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        //             Greeting(
        //                 name = "Android",
        //                 modifier = Modifier.padding(innerPadding)
        //             )
        //         }
        //     }
        // }
    }
}