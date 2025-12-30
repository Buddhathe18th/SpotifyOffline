package com.buddhathe18th.spotifyoffline.common.models

import android.net.Uri
import android.graphics.BitmapFactory
import androidx.annotation.Keep

@Keep
data class Song(
        val title: String,
        val artist: String,
        val artists: List<String>,
        val uri: Uri,
        val durationMs: Long,
        val album: String?,
        val imageAlbumArt: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Song) return false
        return uri == other.uri
    }
}
