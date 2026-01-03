package com.buddhathe18th.spotifyoffline.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SearchPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        SearchResultFragment.newInstance(SearchResultFragment.SearchType.ALL),
        SearchResultFragment.newInstance(SearchResultFragment.SearchType.SONGS),
        SearchResultFragment.newInstance(SearchResultFragment.SearchType.ALBUMS),
        SearchResultFragment.newInstance(SearchResultFragment.SearchType.ARTISTS),
        SearchResultFragment.newInstance(SearchResultFragment.SearchType.PLAYLISTS)
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment{
        return fragments[position]
    }

    fun getFragmentAt(position: Int): SearchResultFragment = fragments[position]
}
