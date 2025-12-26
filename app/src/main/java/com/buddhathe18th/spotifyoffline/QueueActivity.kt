package com.buddhathe18th.spotifyoffline

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class QueueActivity : ComponentActivity() {

    private val playQueue = QueueManager.playQueue
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
                            // Send selected index back to MainActivity
                            setResult(
                                    Activity.RESULT_OK,
                                    Intent().putExtra(EXTRA_JUMP_INDEX, index)
                            )
                            finish()
                        },
                        onRemove = { index ->
                            playQueue.remove(index)
                            //TODO: if removed current song, stop playback
                            // refresh adapter from queue after removal
                            adapter.setData(playQueue.getQueue(), playQueue.getCurrentIndex())
                        }
                )

        recycler.adapter = adapter

        findViewById<Button>(R.id.buttonRefreshQueue).setOnClickListener {
            adapter.setData(playQueue.getQueue(), playQueue.getCurrentIndex())
        }
    }

    companion object {
        const val EXTRA_JUMP_INDEX = "jump_index"
    }
}
