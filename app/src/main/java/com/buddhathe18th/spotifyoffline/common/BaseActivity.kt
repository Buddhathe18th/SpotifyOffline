package com.buddhathe18th.spotifyoffline.common

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.player.MusicPlayerManager
import com.buddhathe18th.spotifyoffline.common.player.PlayQueue
import com.buddhathe18th.spotifyoffline.common.player.QueueManager
import com.buddhathe18th.spotifyoffline.queue.QueueActivity

open class BaseActivity : ComponentActivity() {

    protected val musicPlayer = MusicPlayerManager.musicPlayer
    protected val playQueue = QueueManager.playQueue

    // Player bar views
    private lateinit var seekBarProgress: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var textTotalTime: TextView
    private lateinit var imageAlbumArt: ImageView
    private lateinit var nowPlayingTitle: TextView
    private lateinit var nowPlayingArtist: TextView
    protected lateinit var buttonViewQueue: Button
    protected lateinit var buttonPlayPause: ImageButton
    protected lateinit var buttonNext: ImageButton
    protected lateinit var buttonPrevious: ImageButton
    protected lateinit var buttonShuffle: ImageButton
    protected lateinit var buttonRepeat: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private var isUpdatingProgress = false
    private var isUserDragging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar()
    }

    override fun setContentView(layoutResID: Int) {
        // Inflate base layout with player bar
        val baseLayout = LayoutInflater.from(this).inflate(R.layout.activity_base, null)

        // Inflate child layout into content container
        val contentContainer = baseLayout.findViewById<FrameLayout>(R.id.contentContainer)
        LayoutInflater.from(this).inflate(layoutResID, contentContainer, true)

        super.setContentView(baseLayout)

        initPlayerBar()
    }

    private fun initPlayerBar() {
        // Find all player bar views
        seekBarProgress = findViewById(R.id.seekBarProgress)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        textTotalTime = findViewById(R.id.textTotalTime)
        imageAlbumArt = findViewById(R.id.imageAlbumArt)
        nowPlayingTitle = findViewById(R.id.nowPlayingTitle)
        nowPlayingArtist = findViewById(R.id.nowPlayingArtist)
        buttonViewQueue = findViewById(R.id.buttonViewQueue)
        buttonPlayPause = findViewById(R.id.buttonPlayPause)
        buttonNext = findViewById(R.id.buttonNext)
        buttonPrevious = findViewById(R.id.buttonPrevious)
        buttonShuffle = findViewById(R.id.buttonShuffle)
        buttonRepeat = findViewById(R.id.buttonRepeat)

        setupPlayerControls()
    }

    private val queueLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val playCurrent =
                            result.data?.getBooleanExtra(QueueActivity.EXTRA_PLAY_CURRENT, false)
                                    ?: false

                    // Handle play current FIRST
                    if (playCurrent) {
                        Log.d("MainActivity", "Playing song at current index after removal")
                        if (playQueue.getCurrentSong() != null) {
                            playSongAtCurrentIndex()
                        } else {
                            // Queue is empty, stop playback
                            Log.d("MainActivity", "Queue empty after removal, stopping playback")
                            musicPlayer.stopAndRelease()
                            findViewById<TextView>(R.id.nowPlayingTitle).text = "Nothing playing"
                            findViewById<TextView>(R.id.nowPlayingArtist).text = ""
                            findViewById<ImageButton>(R.id.buttonPlayPause).apply {
                                setImageResource(android.R.drawable.ic_media_play)
                                isEnabled = false
                            }
                            stopProgressUpdates()
                            updateNavigationButtons()
                        }
                        return@registerForActivityResult
                    }

                    // Handle jump index only if playCurrent wasn't set
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

    private fun setupPlayerControls() {
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

        buttonNext.setOnClickListener { playNextSong() }
        buttonPrevious.setOnClickListener { playPreviousSong() }

        buttonShuffle.setOnClickListener {
            playQueue.toggleShuffle()
            updateShuffleButton()
            Log.d("MainActivity", "Shuffle: ${playQueue.isShuffleEnabled()}")
        }

        buttonRepeat.setOnClickListener {
            playQueue.toggleRepeatMode()
            updateRepeatButton()
            Log.d("MainActivity", "Repeat mode: ${playQueue.getRepeatMode()}")
        }

        buttonViewQueue.setOnClickListener {
            queueLauncher.launch(Intent(this, QueueActivity::class.java))
        }

        // SeekBar listener
        seekBarProgress.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                    ) {}

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        isUserDragging = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        isUserDragging = false
                        seekBar?.let { musicPlayer.seekTo(it.progress) }
                    }
                }
        )
    }

    protected fun updatePlayerUI() {
        val song = playQueue.getCurrentSong()

        if (song != null) {
            nowPlayingTitle.text = song.song.title
            nowPlayingArtist.text = song.artistNames

            val albumArtBytes = getEmbeddedAlbumArt(Uri.parse(song.song.uri))
            if (albumArtBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.size)
                imageAlbumArt.setImageBitmap(bitmap)
            } else {
                imageAlbumArt.setImageResource(0)
                imageAlbumArt.setBackgroundColor(android.graphics.Color.DKGRAY)
            }

            buttonPlayPause.setImageResource(
                    if (musicPlayer.isPlaying()) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
            )
            buttonPlayPause.isEnabled = true
            updateNavigationButtons()
        } else {
            nowPlayingTitle.text = "Nothing playing"
            nowPlayingArtist.text = ""
            buttonPlayPause.setImageResource(android.R.drawable.ic_media_play)
            buttonPlayPause.isEnabled = false
        }
    }

    protected fun updateNavigationButtons() {
        buttonNext.isEnabled = playQueue.hasNext()
        buttonPrevious.isEnabled = playQueue.hasPrevious()
        buttonNext.alpha = if (playQueue.hasNext()) 1.0f else 0.5f
        buttonPrevious.alpha = if (playQueue.hasPrevious()) 1.0f else 0.5f
    }

    private fun updateShuffleButton() {
        buttonShuffle.alpha = if (playQueue.isShuffleEnabled()) 1.0f else 0.5f
    }

    private fun updateRepeatButton() {
        buttonRepeat.alpha =
                when (playQueue.getRepeatMode()) {
                    PlayQueue.RepeatMode.NONE -> 0.5f
                    PlayQueue.RepeatMode.ALL -> 1.0f
                    PlayQueue.RepeatMode.ONE -> 1.0f
                }
    }

    protected fun startProgressUpdates() {
        isUpdatingProgress = true
        handler.post(updateProgressRunnable)
    }

    protected fun stopProgressUpdates() {
        isUpdatingProgress = false
        handler.removeCallbacks(updateProgressRunnable)
    }

    private val updateProgressRunnable =
            object : Runnable {
                override fun run() {
                    if (musicPlayer.isPlaying()) {
                        val currentPos = musicPlayer.getCurrentPosition()
                        val duration = musicPlayer.getDuration()

                        if (duration > 0) {
                            seekBarProgress.max = duration
                            if (!isUserDragging) {
                                seekBarProgress.progress = currentPos
                            }
                            textCurrentTime.text = formatTime(currentPos)
                            textTotalTime.text = formatTime(duration)
                        }
                    }

                    if (isUpdatingProgress) {
                        handler.postDelayed(this, 1000)
                    }
                }
            }

    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    protected open fun playNextSong() {
        val nextSong = playQueue.next()
        if (nextSong != null) {
            Log.d("BaseActivity", "Playing next: ${nextSong.song.title}")
            playSongAtCurrentIndex()
        } else {
            Log.d("BaseActivity", "No next song available")
            updateNavigationButtons()
        }
    }

    protected open fun playPreviousSong() {
        val previousSong = playQueue.previous()
        if (previousSong != null) {
            Log.d("BaseActivity", "Playing previous: ${previousSong.song.title}")
            playSongAtCurrentIndex()
        } else {
            Log.d("BaseActivity", "No previous song available")
            updateNavigationButtons()
        }
    }

    protected open fun playSongAtCurrentIndex() {
        val song = playQueue.getCurrentSong() ?: return

        val textTitle = findViewById<TextView>(R.id.nowPlayingTitle)
        val textArtist = findViewById<TextView>(R.id.nowPlayingArtist)
        val buttonPlayPause = findViewById<ImageButton>(R.id.buttonPlayPause)
        val imageAlbumArt = findViewById<ImageView>(R.id.imageAlbumArt)

        musicPlayer.play(
                context = this,
                uri = Uri.parse(song.song.uri),
                onPrepared = {
                    Log.d("MainActivity", "Playback started for ${song.song.title}")
                    val albumArtBytes = getEmbeddedAlbumArt(Uri.parse(song.song.uri))
                    runOnUiThread {
                        textTitle.text = "${song.song.title}"
                        textArtist.text = "${song.artistNames}"
                        if (albumArtBytes != null) {
                            val bitmap =
                                    BitmapFactory.decodeByteArray(
                                            albumArtBytes,
                                            0,
                                            albumArtBytes.size
                                    )
                            imageAlbumArt.setImageBitmap(bitmap)
                        } else {
                            imageAlbumArt.setImageResource(0) // Clear previous image
                            imageAlbumArt.setBackgroundColor(android.graphics.Color.DKGRAY)
                        }

                        buttonPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                        buttonPlayPause.isEnabled = true
                        updateNavigationButtons()
                        startProgressUpdates()
                    }
                },
                onCompletion = {
                    Log.d("MainActivity", "Playback completed for ${song.song.title}")
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

    private fun getEmbeddedAlbumArt(uri: Uri): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(this, uri)
            retriever.embeddedPicture
        } catch (e: Exception) {
            Log.e("BaseActivity", "Could not read album art for $uri", e)
            null
        } finally {
            retriever.release()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePlayerUI()
        if (musicPlayer.isPlaying()) {
            startProgressUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopProgressUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdates()
    }

    @Suppress("DEPRECATION")
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(Color.WHITE)
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = true
        } else {
            window.statusBarColor = Color.WHITE
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = true
        }
    }
}
