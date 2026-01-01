package com.buddhathe18th.spotifyoffline.playlists

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists

class SelectableSongAdapter(
        private var songs: List<SongWithArtists>,
        private val selectedIds: MutableSet<String>,
        private val onClick: (SongWithArtists) -> Unit
) : RecyclerView.Adapter<SelectableSongAdapter.SongViewHolder>() {

    private var filteredSongs: List<SongWithArtists> = songs

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.songTitle)
        val textArtist: TextView = itemView.findViewById(R.id.songArtist)
        val albumArt: ImageView = itemView.findViewById(R.id.songAlbumArt)

        fun bind(song: SongWithArtists) {
            textTitle.text = song.song.title
            textArtist.text = song.artistNames

            val albumArtBytes = getEmbeddedAlbumArt(albumArt.context, Uri.parse(song.song.uri))
            if (albumArtBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.size)
                albumArt.setImageBitmap(bitmap)
            } else {
                albumArt.setImageResource(0)
                albumArt.setBackgroundColor(android.graphics.Color.DKGRAY)
            }

            // Highlight if selected
            itemView.isSelected = selectedIds.contains(song.song.id)

            itemView.setOnClickListener {
                onClick(song)
                // Toggle selection UI
                itemView.isSelected = selectedIds.contains(song.song.id)
            }
        }
    }

    private fun getEmbeddedAlbumArt(context: android.content.Context, uri: Uri): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture
        } catch (e: Exception) {
            Log.e("SelectableSongAdapter", "Could not read album art for $uri", e)
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
        holder.bind(filteredSongs[position])
    }

    override fun getItemCount(): Int = filteredSongs.size

    fun filter(query: String) {
        filteredSongs =
                if (query.isEmpty()) {
                    songs
                } else {
                    songs.filter {
                        it.song.title.contains(query, ignoreCase = true) ||
                                it.artistNames.contains(query, ignoreCase = true)
                    }
                }
        notifyDataSetChanged()
    }

    fun updateSongs(newSongs: List<SongWithArtists>) {
        songs = newSongs
        filteredSongs = newSongs
        notifyDataSetChanged()
    }
}
