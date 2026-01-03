package com.buddhathe18th.spotifyoffline.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.buddhathe18th.spotifyoffline.R
import com.buddhathe18th.spotifyoffline.common.data.AppDatabase
import com.buddhathe18th.spotifyoffline.main.SongWithArtistsAdapter
import kotlinx.coroutines.launch
import android.util.Log


class SearchResultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var emptyMessage: TextView
    private var searchType: SearchType = SearchType.ALL

    enum class SearchType {
        ALL, SONGS, ALBUMS, ARTISTS, PLAYLISTS
    }

    companion object {
        private const val ARG_SEARCH_TYPE = "search_type"

        fun newInstance(searchType: SearchType): SearchResultFragment {
            val fragment = SearchResultFragment()
            val args = Bundle()
            args.putString(ARG_SEARCH_TYPE, searchType.name)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchType = SearchType.valueOf(it.getString(ARG_SEARCH_TYPE, "ALL"))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerResults)
        emptyState = view.findViewById(R.id.layoutEmptyState)
        emptyMessage = view.findViewById(R.id.textEmptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        showEmptyState(true)
    }

    fun search(query: String) {
        if (query.isBlank()) {
            showEmptyState(true)
            emptyMessage.text = "Start typing to search"
            return
        }

        // Perform search based on type
        when (searchType) {
            SearchType.ALL -> searchAll(query)
            SearchType.SONGS -> searchSongs(query)
            SearchType.ALBUMS -> searchAlbums(query)
            SearchType.ARTISTS -> searchArtists(query)
            SearchType.PLAYLISTS -> searchPlaylists(query)
        }
    }

    private fun searchAll(query: String) {
        // val db = AppDatabase.getDatabase(requireContext())
        
        // lifecycleScope.launch {
        //     db.songDao().searchSongsWithArtists(query).collect { songs ->
        //         if (songs.isEmpty()) {
        //             showEmptyState(true)
        //             emptyMessage.text = "No results found"
        //         } else {
        //             showEmptyState(false)
        //             val adapter = SongWithArtistsAdapter(songs.map { it.toSong() }) { song ->
        //                 // Play song
        //             }
        //             recyclerView.adapter = adapter
        //         }
        //     }
        // }
    }

    private fun searchSongs(query: String) {
        val db = AppDatabase.getDatabase(requireContext())
        
        lifecycleScope.launch {
            db.songDao().searchSongsWithArtists(query).collect { songs ->
                Log.d("SearchResultFragment", "Found ${songs.size} songs for query: $query")
                if (songs.isEmpty()) {
                    showEmptyState(true)
                    emptyMessage.text = "No songs found"
                } else {
                    showEmptyState(false)
                    val adapter = SongWithArtistsAdapter(songs) { song ->
                        // Play song
                    }
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    private fun searchAlbums(query: String) {
    //     val db = AppDatabase.getDatabase(requireContext())
        
    //     lifecycleScope.launch {
    //         db.songDao().searchSongsWithArtists(query).collect { songs ->
    //             // Group by album
    //             val albums = songs
    //                 .filter { it.song.album != null }
    //                 .groupBy { it.song.album }
    //                 .map { (album, songList) ->
    //                     AlbumItem(album ?: "Unknown", songList.size)
    //                 }
                
    //             if (albums.isEmpty()) {
    //                 showEmptyState(true)
    //                 emptyMessage.text = "No albums found"
    //             } else {
    //                 showEmptyState(false)
    //                 val adapter = AlbumAdapter(albums) { album ->
    //                     // Show album songs
    //                 }
    //                 recyclerView.adapter = adapter
    //             }
    //         }
    //     }
    }

    private fun searchArtists(query: String) {
    //     val db = AppDatabase.getDatabase(requireContext())
        
    //     lifecycleScope.launch {
    //         db.artistDao().searchArtistsByName(query).collect { artists ->
    //             if (artists.isEmpty()) {
    //                 showEmptyState(true)
    //                 emptyMessage.text = "No artists found"
    //             } else {
    //                 showEmptyState(false)
    //                 val adapter = ArtistAdapter(artists) { artist ->
    //                     // Show artist songs
    //                 }
    //                 recyclerView.adapter = adapter
    //             }
    //         }
    //     }
    }

    private fun searchPlaylists(query: String) {
    //     val db = AppDatabase.getDatabase(requireContext())
        
    //     lifecycleScope.launch {
    //         db.playlistDao().getAllPlaylistsWithCount().collect { playlists ->
    //             val filtered = playlists.filter { 
    //                 it.name.contains(query, ignoreCase = true) 
    //             }
                
    //             if (filtered.isEmpty()) {
    //                 showEmptyState(true)
    //                 emptyMessage.text = "No playlists found"
    //             } else {
    //                 showEmptyState(false)
    //                 val adapter = PlaylistAdapter(filtered) { playlist ->
    //                     // Open playlist
    //                 }
    //                 recyclerView.adapter = adapter
    //             }
    //         }
    //     }
    }

    private fun showEmptyState(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}
