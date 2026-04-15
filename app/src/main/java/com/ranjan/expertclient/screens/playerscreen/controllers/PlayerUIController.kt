package com.ranjan.expertclient.screens.playerscreen.controllers

import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.utils.formatTime

class PlayerUIController(
    private val binding: PlayerScreenBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val psv: PlayerScreenViewModel,
    private val playerManager: PlayerManager
) {

    fun setup() {

        binding.playerUI.imageView14.setOnClickListener {
            playerManager.togglePlayback()
        }

        binding.playerView.setOnClickListener {
            toggleControls()
        }

        binding.playerUI.linearLayout.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        binding.playerUI.textView12.text = formatTime(progress.toLong())
                        playerManager.seekTo(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )

        observe()
    }

    private fun observe() {

        psv.totalDuration.observe(lifecycleOwner) {
            binding.playerUI.textView13.text = formatTime(it ?: 0)
            binding.playerUI.linearLayout.max = (it ?: 0).toInt()
        }

        psv.durationProgress.observe(lifecycleOwner) {
            binding.playerUI.textView12.text = formatTime(it ?: 0)
            binding.playerUI.linearLayout.progress = (it ?: 0).toInt()
        }

        psv.isPaused.observe(lifecycleOwner) {
            binding.playerUI.imageView14.setImageResource(
                if (it) R.drawable.play else R.drawable.pause
            )
        }

        psv.showControls.observe(lifecycleOwner) {
            binding.playerUI.root.visibility =
                if (it) View.VISIBLE else View.GONE
        }

        psv.isPaused.observe(lifecycleOwner) {
            updateLoadingIndicator()
        }
    }

    private fun toggleControls() {
        val current = psv.showControls.value ?: true
        psv.showControls.postValue(!current)
    }
    fun updateLoadingIndicator() {
        val show = (psv.isLoading.value == true) || (psv.isPaused.value == true)
        binding.loadingIndicator.visibility =
            if (show) View.VISIBLE else View.GONE
    }
}