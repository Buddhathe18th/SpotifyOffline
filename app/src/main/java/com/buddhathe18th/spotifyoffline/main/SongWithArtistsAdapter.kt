package com.buddhathe18th.spotifyoffline.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists

class SongWithArtistsAdapter(
        private var songs: List<SongWithArtists>,
        private val onClick: (SongWithArtists) -> Unit
) : RecyclerView.Adapter<SongWithArtistsAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.songTitle)
        val textArtist: TextView = itemView.findViewById(R.id.songArtist)
        //  val albumArt: ImageView = itemView.findViewById(R.id.songAlbumArt)

        fun bind(songWithArtists: SongWithArtists) {
            textTitle.text = songWithArtists.song.title
            textArtist.text = songWithArtists.artistNames // Uses the helper property!

            // song.imageAlbumArt?.let { byteArray ->
            //     val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            //     albumArt.setImageBitmap(bitmap)
            // }
            //         ?: run { albumArt.setBackgroundColor(android.graphics.Color.DKGRAY) }

            itemView.setOnClickListener { onClick(songWithArtists) }
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
}
