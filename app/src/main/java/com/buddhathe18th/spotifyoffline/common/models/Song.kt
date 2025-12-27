package com.buddhathe18th.spotifyoffline.common.models

import android.net.Uri
import androidx.annotation.Keep

@Keep
data class Song(
    val title: String,
    val artist: String,
    val artists: List<String>,
    val uri: Uri,
    val durationMs: Long
)
