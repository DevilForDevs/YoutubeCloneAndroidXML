package com.ranjan.expertclient.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ranjan.expertclient.databinding.BottomNavScreenBinding


class BottomNavScreen: Fragment() {
    private lateinit var binding: BottomNavScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomNavScreenBinding.inflate(inflater, container, false)
        val navController = binding.root.findNavController()
        binding.bottomNavigation.setupWithNavController(navController)
        return  binding.root


    }

}