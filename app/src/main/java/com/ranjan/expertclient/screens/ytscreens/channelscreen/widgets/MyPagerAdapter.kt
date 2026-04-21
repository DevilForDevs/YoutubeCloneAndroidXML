package com.ranjan.expertclient.screens.ytscreens.channelscreen.widgets

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ranjan.expertclient.screens.ytscreens.channelscreen.models.ChannelTab
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist.PlaylistFragment
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.shorts.ShortsFragment
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.videos.VideosFragment

class MyPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> VideosFragment()
            1 -> ShortsFragment()
            else -> PlaylistFragment()
        }
    }
}