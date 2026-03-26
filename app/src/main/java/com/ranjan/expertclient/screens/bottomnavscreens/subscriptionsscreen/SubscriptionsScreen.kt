package com.ranjan.expertclient.screens.bottomnavscreens.subscriptionsscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ranjan.expertclient.databinding.SubscriptionsScreenBinding

class SubscriptionsScreen : Fragment() {

    private lateinit var binding: SubscriptionsScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SubscriptionsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

}