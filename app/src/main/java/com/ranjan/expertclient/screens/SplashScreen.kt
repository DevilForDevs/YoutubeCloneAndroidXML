package com.ranjan.expertclient.screens

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.databinding.SplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ranjan.expertclient.R

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment() {
    private lateinit var binding: SplashScreenBinding
//    private val backThread = CoroutineScope(Dispatchers.IO)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplashScreenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(3000)
            findNavController().navigate(R.id.action_splashScreen_to_sitesChooserScreen)
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                findNavController().navigate(R.id.action_splashScreen_to_browserScreen)
            } else {
                findNavController().navigate(R.id.action_splashScreen_to_bottomNavScreen)
            }*/
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }



}