package com.buddhathe18th.spotifyoffline

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class QueueAdapter(
    private val songs: MutableList<Song>,
    private var currentIndex: Int,
    private val onTap: (index: Int) -> Unit,
    private val onRemove: (index: Int) -> Unit
) : RecyclerView.Adapter<QueueAdapter.VH>() {

    fun setData(newSongs: List<Song>, newCurrentIndex: Int) {
        songs.clear()
        songs.addAll(newSongs)
        currentIndex = newCurrentIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_queue_song, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val song = songs[position]
        holder.textTitle.text = "${song.title} - ${song.artist}"

        // Highlight currently playing row
        val isCurrent = position == currentIndex
        holder.root.setBackgroundColor(
            if (isCurrent) ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light)
            else ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
        )
        holder.textTitle.setTypeface(null, if (isCurrent) Typeface.BOLD else Typeface.NORMAL)

        holder.root.setOnClickListener { onTap(position) }
        holder.buttonRemove.setOnClickListener {
            // Remove instantly from adapter list for smooth UI
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
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)
    }
}
