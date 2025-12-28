package com.buddhathe18th.spotifyoffline.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowInsets
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.MediaStoreSongRepository
import com.buddhathe18th.spotifyoffline.common.models.Song
import com.buddhathe18th.spotifyoffline.common.player.MusicPlayer
import com.buddhathe18th.spotifyoffline.common.player.PlayQueue
import com.buddhathe18th.spotifyoffline.common.player.QueueManager
import com.buddhathe18th.spotifyoffline.common.BaseActivity
import com.buddhathe18th.spotifyoffline.queue.QueueActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {

    private val musicPlayer = MusicPlayer()
    private val playQueue = QueueManager.playQueue

    private val handler = Handler(Looper.getMainLooper())
    private var isUpdatingProgress = false

    private val queueLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {

                    val jumpIndex =
                            result.data?.getIntExtra(QueueActivity.EXTRA_JUMP_INDEX, -1) ?: -1
                    Log.d(
                            "MainActivity",
                            "Jumping to index from queue: ${jumpIndex} to song ${playQueue.getQueue()[jumpIndex].title}"
                    )

                    if (jumpIndex >= 0) {

                        playQueue.setCurrentIndex(jumpIndex)
                        playSongAtCurrentIndex()
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val buttonPlayPause = findViewById<ImageButton>(R.id.buttonPlayPause)
        val buttonNext = findViewById<ImageButton>(R.id.buttonNext)
        val buttonPrevious = findViewById<ImageButton>(R.id.buttonPrevious)
        val buttonShuffle = findViewById<ImageButton>(R.id.buttonShuffle)
        val buttonRepeat = findViewById<ImageButton>(R.id.buttonRepeat)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        var adapter =
                SongAdapter(emptyList()) { song -> Log.d("MainActivity", "Clicked: ${song.title}") }
        val scanButton = findViewById<Button>(R.id.buttonScan)
        val buttonViewQueue = findViewById<Button>(R.id.buttonViewQueue)

        buttonPlayPause.isEnabled = false
        buttonNext.isEnabled = false
        buttonPrevious.isEnabled = false

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

        // Next button
        buttonNext.setOnClickListener { playNextSong() }

        // Previous button
        buttonPrevious.setOnClickListener { playPreviousSong() }

        // Shuffle button
        buttonShuffle.setOnClickListener {
            playQueue.toggleShuffle()
            updateShuffleButton(buttonShuffle)
            Log.d("MainActivity", "Shuffle: ${playQueue.isShuffleEnabled()}")
        }

        // Repeat button
        buttonRepeat.setOnClickListener {
            playQueue.toggleRepeatMode()
            updateRepeatButton(buttonRepeat)
            Log.d("MainActivity", "Repeat mode: ${playQueue.getRepeatMode()}")
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

        buttonViewQueue.setOnClickListener {
            queueLauncher.launch(Intent(this, QueueActivity::class.java))
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

                    // Find the index of the clicked song and set the queue to start there
                    val clickedIndex = songs.indexOf(song)
                    if (clickedIndex >= 0) {
                        val currentQueue = playQueue.getQueue()
                        if (song == playQueue.getCurrentSong()) {
                            Log.d("MainActivity", "Song: ${song.title} is already playing")
                            musicPlayer.restartCurrentSong()
                        } else if (currentQueue.contains(song)) {
                            Log.d("MainActivity", "Song: ${song.title} is already in queue")
                            val indexInQueue = currentQueue.indexOf(song)
                            playQueue.setCurrentIndex(indexInQueue)
                        } else {
                            Log.d("MainActivity", "Song: ${song.title} is a new song in queue")

                            playQueue.addToQueue(0, song)
                            playQueue.setQueue(playQueue.getQueue(), 0)
                        }
                        playSongAtCurrentIndex()
                        Log.d("MainActivity", "Queue: ${playQueue.getQueue()}")
                    }
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

                            seekBar.setOnSeekBarChangeListener(
                                    object : SeekBar.OnSeekBarChangeListener {
                                        override fun onProgressChanged(
                                                seekBar: SeekBar?,
                                                progress: Int,
                                                fromUser: Boolean
                                        ) {
                                            if (fromUser) {
                                                musicPlayer.seekTo(progress)
                                            }
                                        }

                                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                            // Optional: pause updates while user is dragging
                                        }

                                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                            // Optional: resume updates after user releases
                                        }
                                    }
                            )

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

    private fun playSongAtCurrentIndex() {
        val song = playQueue.getCurrentSong() ?: return

        val textTitle = findViewById<TextView>(R.id.nowPlayingTitle)
        val textArtist = findViewById<TextView>(R.id.nowPlayingArtist)
        val buttonPlayPause = findViewById<ImageButton>(R.id.buttonPlayPause)

        musicPlayer.play(
                context = this,
                uri = song.uri,
                onPrepared = {
                    Log.d("MainActivity", "Playback started for ${song.title}")
                    runOnUiThread {
                        textTitle.text = "${song.title}"
                        textArtist.text = "${song.artists.joinToString(", ")}"
                        buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                        buttonPlayPause.isEnabled = true
                        updateNavigationButtons()
                        startProgressUpdates()
                    }
                },
                onCompletion = {
                    Log.d("MainActivity", "Playback completed for ${song.title}")
                    runOnUiThread {
                        if (playQueue.hasNext()) {
                            playNextSong()
                        } else {
                            textTitle.text = "Nothing playing"
                            textArtist.text = ""

                            buttonPlayPause.setImageResource(android.R.drawable.ic_media_play)
                            buttonPlayPause.isEnabled = false
                            stopProgressUpdates()
                            updateNavigationButtons()
                        }
                    }
                }
        )
    }

    private fun playNextSong() {
        val nextSong = playQueue.next()
        if (nextSong != null) {
            Log.d("MainActivity", "Playing next: ${nextSong.title}")
            playSongAtCurrentIndex()
        } else {
            Log.d("MainActivity", "No next song available")
            updateNavigationButtons()
        }
    }

    private fun playPreviousSong() {
        val previousSong = playQueue.previous()
        if (previousSong != null) {
            Log.d("MainActivity", "Playing previous: ${previousSong.title}")
            playSongAtCurrentIndex()
        } else {
            Log.d("MainActivity", "No previous song available")
            updateNavigationButtons()
        }
    }

    private fun updateNavigationButtons() {
        val buttonNext = findViewById<ImageButton>(R.id.buttonNext)
        val buttonPrevious = findViewById<ImageButton>(R.id.buttonPrevious)

        buttonNext.isEnabled = playQueue.hasNext()
        buttonPrevious.isEnabled = playQueue.hasPrevious()

        // Visual feedback for disabled buttons
        buttonNext.alpha = if (playQueue.hasNext()) 1.0f else 0.5f
        buttonPrevious.alpha = if (playQueue.hasPrevious()) 1.0f else 0.5f
    }

    private fun updateShuffleButton(button: ImageButton) {
        button.alpha = if (playQueue.isShuffleEnabled()) 1.0f else 0.5f
    }

    private fun updateRepeatButton(button: ImageButton) {
        when (playQueue.getRepeatMode()) {
            PlayQueue.RepeatMode.NONE -> button.alpha = 0.5f
            PlayQueue.RepeatMode.ALL -> button.alpha = 1.0f
            PlayQueue.RepeatMode.ONE -> button.alpha = 1.0f
        }
    }
}
