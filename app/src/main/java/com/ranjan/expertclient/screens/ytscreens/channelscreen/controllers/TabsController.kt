package com.ranjan.expertclient.screens.ytscreens.channelscreen.controllers

import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.ChannelScreenBinding
import com.ranjan.expertclient.screens.ytscreens.channelscreen.ChannelScreenViewModel
import com.ranjan.expertclient.screens.ytscreens.channelscreen.widgets.MyPagerAdapter

class TabsController(
    private val binding: ChannelScreenBinding,
    private val fragment: Fragment,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: ChannelScreenViewModel
) {

    private var mediator: TabLayoutMediator? = null
    private val tabs=listOf("Videos","Shorts","Playlist")



    fun setup() {

        val adapter = MyPagerAdapter(fragment)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1
        mediator = TabLayoutMediator(binding.tabview, binding.viewPager) { tab, position ->
            val customView = LayoutInflater.from(fragment.requireContext())
                .inflate(R.layout.custom_tab_header, binding.tabview, false)

            customView.findViewById<TextView>(R.id.tab_text).apply {
                text = tabs[position]
                isSelected = position == 0
            }

            tab.customView = customView
        }

        mediator?.attach()

        binding.tabview.addOnTabSelectedListener(tabListener)

        viewModel.channelMetaData.observe(lifecycleOwner){channelMetaData ->
            binding.textView30.text= channelMetaData?.title
            Glide.with(binding.imageView30)
                .load(channelMetaData?.channelAvatar)
                .circleCrop()
                .into(binding.imageView30)
            binding.textView31.text= channelMetaData?.totalVideos
        }


    }

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.customView?.findViewById<TextView>(R.id.tab_text)
                ?.isSelected = true
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            tab?.customView?.findViewById<TextView>(R.id.tab_text)
                ?.isSelected = false
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    fun clear() {
        mediator?.detach()
        binding.tabview.removeOnTabSelectedListener(tabListener)
    }
}