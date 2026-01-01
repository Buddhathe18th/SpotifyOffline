package com.buddhathe18th.spotifyoffline.playlists

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistSongAdapter(
        private var songs: List<SongWithArtists>,
        private val onClick: (SongWithArtists) -> Unit,
        private val onRemove: (SongWithArtists) -> Unit
) : RecyclerView.Adapter<PlaylistSongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.songTitle)
        val textArtist: TextView = itemView.findViewById(R.id.songArtist)
        val albumArt: ImageView = itemView.findViewById(R.id.songAlbumArt)
        val buttonRemove: ImageButton = itemView.findViewById(R.id.buttonRemoveSong)

        fun bind(song: SongWithArtists) {
            textTitle.text = song.song.title
            textArtist.text = song.artistNames

            albumArt.setBackgroundColor(android.graphics.Color.DKGRAY)

            CoroutineScope(Dispatchers.IO).launch {
                val albumArtBytes = getEmbeddedAlbumArt(albumArt.context, Uri.parse(song.song.uri))
                withContext(Dispatchers.Main) {
                    if (albumArtBytes != null) {
                        val bitmap =
                                BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.size)
                        albumArt.setImageBitmap(bitmap)
                    } else {
                        albumArt.setImageResource(0)
                        albumArt.setBackgroundColor(android.graphics.Color.DKGRAY)
                    }
                }
            }

            buttonRemove.visibility = View.VISIBLE
            itemView.setOnClickListener { onClick(song) }
            buttonRemove.setOnClickListener { onRemove(song) }
        }
    }

    private fun getEmbeddedAlbumArt(context: android.content.Context, uri: Uri): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture
        } catch (e: Exception) {
            Log.e("PlaylistSongAdapter", "Could not read album art for $uri", e)
            null
        } finally {
            retriever.release()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_playlist_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    fun updateSongs(newSongs: List<SongWithArtists>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    fun getCurrentSongs(): List<SongWithArtists> {
        return songs
    }
}
