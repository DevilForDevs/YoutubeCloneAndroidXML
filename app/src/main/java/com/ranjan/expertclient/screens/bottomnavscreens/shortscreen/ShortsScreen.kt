package com.ranjan.expertclient.screens.bottomnavscreens.shortscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ranjan.expertclient.databinding.ShortsScreenBinding

class ShortsScreen : Fragment() {

    private lateinit var binding: ShortsScreenBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ShortsScreenBinding.inflate(inflater, container, false)

        return binding.root
    }



}