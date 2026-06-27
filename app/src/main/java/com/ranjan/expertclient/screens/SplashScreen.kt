package com.ranjan.expertclient.screens

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.databinding.SplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ranjan.expertclient.R
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import kotlin.getValue

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment() {
    private lateinit var binding: SplashScreenBinding
//    private val backThread = CoroutineScope(Dispatchers.IO)
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()

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
            if (sharedViewModel.selectedVideo.value!=null){
                if (sharedViewModel.selectedVideo.value!!.title=="From Intent"){
                    findNavController().navigate(R.id.action_splashScreen_to_playerScreen)
                }else{
                    findNavController().navigate(R.id.action_splashScreen_to_sitesChooserScreen)
                }
            }else{
                findNavController().navigate(R.id.action_splashScreen_to_sitesChooserScreen)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }



}