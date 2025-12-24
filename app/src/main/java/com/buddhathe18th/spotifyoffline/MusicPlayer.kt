package com.buddhathe18th.spotifyoffline

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

class MusicPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentUri: Uri? = null
    private var isPrepared: Boolean = false

    fun play(context: Context, uri: Uri, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        // If same Uri is already playing, ignore or just resume
        if (uri == currentUri && mediaPlayer != null && isPrepared) {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
            }
            onPrepared()
            return
        }

        stopAndRelease()

        currentUri = uri
        isPrepared = false

        val mp = MediaPlayer()
        mediaPlayer = mp

        mp.setAudioAttributes(
                AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        )

        mp.setDataSource(context, uri)

        mp.setOnPreparedListener {
            isPrepared = true
            it.start()
            onPrepared()
        }

        mp.setOnCompletionListener {
            Log.d("MusicPlayer", "Playback completed")
            onCompletion()
        }

        mp.setOnErrorListener { _, what, extra ->
            Log.e("MusicPlayer", "MediaPlayer error: what=$what extra=$extra")
            stopAndRelease()
            true
        }

        // Async prepare so UI thread is not blocked
        mp.prepareAsync()
    }

    fun pause() {
        val mp = mediaPlayer
        if (mp != null && isPrepared && mp.isPlaying) {
            mp.pause()
        }
    }

    fun resume() {
        val mp = mediaPlayer
        if (mp != null && isPrepared && !mp.isPlaying) {
            mp.start()
        }
    }

    fun stopAndRelease() {
        val mp = mediaPlayer
        if (mp != null) {
            try {
                mp.stop()
            } catch (e: IllegalStateException) {
                // Ignore if already stopped
            }
            mp.reset()
            mp.release()
        }
        mediaPlayer = null
        currentUri = null
        isPrepared = false
    }

    fun isPlaying(): Boolean {
        val mp = mediaPlayer
        return mp != null && isPrepared && mp.isPlaying
    }
}
