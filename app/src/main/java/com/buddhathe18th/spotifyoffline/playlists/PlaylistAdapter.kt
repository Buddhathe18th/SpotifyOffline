package com.buddhathe18th.spotifyoffline.playlists

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import com.buddhathe18th.spotifyoffline.common.data.database.PlaylistWithSongCount
import com.buddhathe18th.spotifyoffline.R

class PlaylistAdapter(
    private var playlists: List<PlaylistWithSongCount>,
    private val onClick: (PlaylistWithSongCount) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    // Color palette for playlist covers (similar to InShot style)
    private val colors = listOf(
        "#FF6B9D", "#C44569", "#F8B500", "#3B3B98",
        "#00D2FF", "#6BCB77", "#FF6464", "#A685E2",
        "#4E9F3D", "#FD7272", "#6A5ACD", "#20B2AA"
    )

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCover: ImageView = itemView.findViewById(R.id.imagePlaylistCover)
        val textName: TextView = itemView.findViewById(R.id.textPlaylistName)
        val textCount: TextView = itemView.findViewById(R.id.textSongCount)
        val textDate: TextView = itemView.findViewById(R.id.textPlaylistDate)

        fun bind(playlist: PlaylistWithSongCount) {
            textName.text = playlist.name
            textCount.text = "${playlist.songCount} songs"
            
            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            textDate.text = "Created ${dateFormat.format(Date(playlist.createdAt))}"
            
            // Set color based on position
            val color = Color.parseColor(colors[bindingAdapterPosition % colors.size])
            imageCover.setBackgroundColor(color)
            
            // Click listener
            itemView.setOnClickListener { onClick(playlist) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_card, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<PlaylistWithSongCount>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}
