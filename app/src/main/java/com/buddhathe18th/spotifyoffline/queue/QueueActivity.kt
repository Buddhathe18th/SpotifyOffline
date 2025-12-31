package com.buddhathe18th.spotifyoffline.queue

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.BaseActivity
import com.buddhathe18th.spotifyoffline.common.player.MusicPlayerManager
import com.buddhathe18th.spotifyoffline.common.player.QueueManager

class QueueActivity : BaseActivity() {

    private lateinit var adapter: QueueAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        val recycler = findViewById<RecyclerView>(R.id.recyclerQueue)
        recycler.layoutManager = LinearLayoutManager(this)
        val backButton = findViewById<Button>(R.id.buttonBackToMain)
        backButton.setOnClickListener { finish() }

        adapter =
                QueueAdapter(
                        songs = playQueue.getQueue().toMutableList(),
                        currentIndex = playQueue.getCurrentIndex(),
                        onTap = { index ->
                            Log.d(
                                    "QueueActivity",
                                    "Jumping to index: ${index} song ${playQueue.getQueue()[index].song.title}"
                            )
                            setResult(
                                    Activity.RESULT_OK,
                                    Intent().putExtra(EXTRA_JUMP_INDEX, index)
                            )
                            finish()
                        },
                        onRemove = { index ->
                            Log.d(
                                    "QueueActivity",
                                    "Removed index: $index, currentIndex now: ${playQueue.getCurrentIndex()}"
                            )

                            if (index == playQueue.getCurrentIndex()) {
                                if (index == playQueue.size() - 1) {
                                    // If last song is removed, move current index back by one
                                    playQueue.remove(index)
                                } else {
                                    playQueue.remove(index)
                                    playQueue.setQueue(
                                            playQueue.getQueue(),
                                            playQueue.getCurrentIndex() + 1
                                    )
                                }
                                setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(EXTRA_PLAY_CURRENT, true)
                                )
                                finish()
                            } else {
                                adapter.setData(playQueue.getQueue(), playQueue.getCurrentIndex())
                            }
                        }
                )

        recycler.adapter = adapter

        findViewById<Button>(R.id.buttonRefreshQueue).setOnClickListener {
            adapter.setData(playQueue.getQueue(), playQueue.getCurrentIndex())
        }
    }

    companion object {
        const val EXTRA_JUMP_INDEX = "jump_index"
        const val EXTRA_PLAY_CURRENT = "play_current"
    }
}
