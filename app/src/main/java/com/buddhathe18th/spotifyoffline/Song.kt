package com.buddhathe18th.spotifyoffline

import android.net.Uri
import androidx.annotation.Keep

@Keep
data class Song(
    val title: String,
    val artist: String,
    val uri: Uri,
    val durationMs: Long
)
