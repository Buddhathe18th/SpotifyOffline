package com.buddhathe18th.spotifyoffline.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.ImageView 

class SongWithArtistsAdapter(
        private var songs: List<SongWithArtists>,
        private val onClick: (SongWithArtists) -> Unit
) : RecyclerView.Adapter<SongWithArtistsAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.songTitle)
        val textArtist: TextView = itemView.findViewById(R.id.songArtist)
         val albumArt: ImageView = itemView.findViewById(R.id.songAlbumArt)

        fun bind(songWithArtists: SongWithArtists) {
            textTitle.text = songWithArtists.song.title
            textArtist.text = songWithArtists.artistNames // Uses the helper property!

            val albumArtBytes =
                    getEmbeddedAlbumArt(albumArt.context, Uri.parse(songWithArtists.song.uri))
            if (albumArtBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.size)
                albumArt.setImageBitmap(bitmap)
            } else {
                albumArt.setImageResource(0) // Clear previous image
                albumArt.setBackgroundColor(android.graphics.Color.DKGRAY)
            }

            itemView.setOnClickListener { onClick(songWithArtists) }
        }
    }

    private fun getEmbeddedAlbumArt(context: android.content.Context, uri: Uri): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture // Returns ByteArray or null
        } catch (e: Exception) {
            Log.e("QueueAdapter", "Could not read album art for $uri", e)
            null
        } finally {
            retriever.release()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    // Important: Method to update the list when database changes
    fun updateSongs(newSongs: List<SongWithArtists>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    fun getCurrentSongs(): List<SongWithArtists> {
        return songs
    }
}
