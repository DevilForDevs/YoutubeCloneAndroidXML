package com.ranjan.expertclient.screens.bottomnavscreens.addscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ranjan.expertclient.databinding.AddScreenBinding

class AddScreen : Fragment() {

    private lateinit var binding: AddScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

}