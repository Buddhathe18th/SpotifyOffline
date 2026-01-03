package com.buddhathe18th.spotifyoffline.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.buddhathe18th.spotifyoffline.R

class MainFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // setContentView(R.layout.activity_main)

        // val recyclerView = findViewById<RecyclerView>(R.id.recyclerSongs)
        // val scanButton = findViewById<Button>(R.id.buttonScan)
        // val buttonViewPlaylists = findViewById<Button>(R.id.buttonViewPlaylists)
        // val buttonSearch = findViewById<Button>(R.id.buttonSearch)

        //     // Initialize adapter with empty list and click handler
        // songAdapter =
        //             SongWithArtistsAdapter(emptyList()) { songWithArtists ->
        //                 Log.d("MainActivity", "Clicked: ${songWithArtists.song.title}")

        //                 val currentQueue = playQueue.getQueue()
        //                 if (songWithArtists == playQueue.getCurrentSong()) {
        //                     Log.d(
        //                             "MainActivity",
        //                             "Song: ${songWithArtists.song.title} is already playing"
        //                     )
        //                     musicPlayer.restartCurrentSong()
        //                 } else if (currentQueue.contains(songWithArtists)) {
        //                     Log.d(
        //                             "MainActivity",
        //                             "Song: ${songWithArtists.song.title} is already in queue"
        //                     )
        //                     val indexInQueue = currentQueue.indexOf(songWithArtists)
        //                     playQueue.setCurrentIndex(indexInQueue)
        //                 } else {
        //                     Log.d(
        //                             "MainActivity",
        //                             "Song: ${songWithArtists.song.title} is a new song in queue"
        //                     )

        //                     if (playQueue.size() == 0) {
        //                         playQueue.addToQueue(0, songWithArtists)
        //                         playQueue.setQueue(playQueue.getQueue(), 0)
        //                     } else {
        //                         playQueue.addToQueue(playQueue.getCurrentIndex() + 1,
        // songWithArtists)
        //                         playQueue.setQueue(
        //                                 playQueue.getQueue(),
        //                                 playQueue.getCurrentIndex() + 1
        //                         )
        //                     }
        //                 }
        //                 playSongAtCurrentIndex()
        //             }

        //     recyclerView.layoutManager = LinearLayoutManager(this)
        //     recyclerView.adapter = songAdapter

        //     scanButton.setOnClickListener {
        //         Log.d("MainActivity", "Scan button clicked")

        //         lifecycleScope.launch {
        //             val repository = SongCacheRepository(this@MainActivity)
        //             try {
        //                 repository.syncFromMediaStore(this@MainActivity)
        //                 Log.d("MainActivity", "Scan complete!")
        //             } catch (e: Exception) {
        //                 Log.e("MainActivity", "Scan failed", e)
        //             }
        //         }
        //     }

        //     buttonViewPlaylists.setOnClickListener {
        //         startActivity(Intent(this, PlaylistActivity::class.java))
        //     }

        //     buttonSearch.setOnClickListener { startActivity(Intent(this,
        // SearchActivity::class.java))

        //     }
    }
}
