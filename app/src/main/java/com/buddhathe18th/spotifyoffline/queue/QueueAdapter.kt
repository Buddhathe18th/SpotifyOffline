package com.buddhathe18th.spotifyoffline.queue

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.database.SongWithArtists

class QueueAdapter(
        private val songs: MutableList<SongWithArtists>,
        private var currentIndex: Int,
        private val onTap: (index: Int) -> Unit,
        private val onRemove: (index: Int) -> Unit
) : RecyclerView.Adapter<QueueAdapter.VH>() {

    fun setData(newSongs: List<SongWithArtists>, newCurrentIndex: Int) {
        songs.clear()
        songs.addAll(newSongs)
        currentIndex = newCurrentIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_queue_song, parent, false)
        return VH(view)
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

    override fun onBindViewHolder(holder: VH, position: Int) {
    val song = songs[position]
    holder.textTitle.text = song.song.title
    holder.textArtist.text = song.artistNames

    // Use Glide - handles everything in background!
    Glide.with(holder.itemView.context)
        .load(Uri.parse(song.song.uri))
        .placeholder(android.R.color.darker_gray)
        .error(android.R.color.darker_gray)
        .into(holder.albumImage)

    // Highlight currently playing row
    val isCurrent = position == currentIndex
    holder.root.setBackgroundColor(
        if (isCurrent)
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light)
        else ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
    )
    holder.textTitle.setTypeface(null, if (isCurrent) Typeface.BOLD else Typeface.NORMAL)

    holder.root.setOnClickListener { onTap(position) }
    holder.buttonRemove.setOnClickListener {
        val removedPos = holder.bindingAdapterPosition
        if (removedPos != RecyclerView.NO_POSITION) {
            songs.removeAt(removedPos)
            notifyItemRemoved(removedPos)
            notifyItemRangeChanged(removedPos, songs.size)
            onRemove(removedPos)
        }
    }
}


    override fun getItemCount(): Int = songs.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: LinearLayout = itemView.findViewById(R.id.root)
        val albumImage: ImageView = itemView.findViewById(R.id.queueSongAlbumArt)
        val textTitle: TextView = itemView.findViewById(R.id.queueSongTitle)
        val textArtist: TextView = itemView.findViewById(R.id.queueSongArtist)
        val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)
    }
}
