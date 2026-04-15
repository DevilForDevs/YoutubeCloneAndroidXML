package com.ranjan.expertclient.screens.playerscreen.controllers

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel

class FullscreenManager(
    private val activity: Activity,
    private val binding: PlayerScreenBinding,
    private val psv: PlayerScreenViewModel,
    private val lifecycleOwner: LifecycleOwner
) {

    fun setup() {

        psv.isFullScreen.observe(lifecycleOwner) { isFull ->
            applyOrientation(isFull)
            updateUI(isFull)
        }
        binding.playerUI.imageView13.setOnClickListener {
            toggleFullScreen()
        }

    }

    private fun applyOrientation(isFull: Boolean) {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (isFull) {
            activity.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun updateUI(isFull: Boolean) {
        binding.playerContainer.layoutParams.height =
            if (isFull) ViewGroup.LayoutParams.MATCH_PARENT else 250.dp

        binding.playerUI.imageView13.setImageResource(
            if (isFull) R.drawable.baseline_fullscreen_exit_24
            else R.drawable.baseline_fullscreen_24
        )
        if (isFull){
            val params = binding.playerUI.linearLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = 20.dp
            binding.playerUI.linearLayout.layoutParams = params
        }else{
            val params = binding.playerUI.linearLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = 0.dp
            binding.playerUI.linearLayout.layoutParams = params
        }


    }

    private fun toggleFullScreen() {
        psv.isFullScreen.value = !(psv.isFullScreen.value ?: false)
    }
}