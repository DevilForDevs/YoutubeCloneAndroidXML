package com.ranjan.expertclient.screens.bottomnavscreens.libraryscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ranjan.expertclient.databinding.LibraryScreenBinding

class LibraryScreen : Fragment() {

    private lateinit var binding: LibraryScreenBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibraryScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
}