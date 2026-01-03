package com.buddhathe18th.spotifyoffline.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.buddhathe18th.spotifyoffline.main.MainFragment
import com.buddhathe18th.spotifyoffline.playlists.PlaylistsFragment
import com.buddhathe18th.spotifyoffline.search.SearchFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainFragment()
            1 -> PlaylistsFragment()
            2 -> SearchFragment()
            else -> MainFragment()
        }
    }
}
