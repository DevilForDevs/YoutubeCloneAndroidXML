package com.ranjan.expertclient.screens.playerscreen.controllers

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.utils.formatTime

class PlayerUIController @OptIn(UnstableApi::class) constructor
    (
    private val binding: PlayerScreenBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val psv: PlayerScreenViewModel,
    private val playerManager: PlayerManager,
    private val activity: FragmentActivity
) {

    private val audioManager by lazy {
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @OptIn(UnstableApi::class)
    fun setup() {

        binding.playerUI.imageView14.setOnClickListener {
            playerManager.togglePlayback()
        }

        binding.playerView.setOnClickListener {
            toggleControls()
        }

        setupDoubleTapToSeek()
        setupPlaybackSeekBar()
        setupVolumeSeekBar()
        setupBrightnessSeekBar()

        observe()
    }

    private fun setupDoubleTapToSeek() {
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            @OptIn(UnstableApi::class)
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val viewWidth = binding.playerContainer.width
                if (e.x < viewWidth / 2) {
                    // Rewind 10s
                    val newPos = (playerManager.player.currentPosition - 10000).coerceAtLeast(0)
                    playerManager.seekTo(newPos)
                } else {
                    // Forward 10s
                    val newPos = (playerManager.player.currentPosition + 10000).coerceAtMost(playerManager.player.duration)
                    playerManager.seekTo(newPos)
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleControls()
                return true
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                // Potential place for performClick if needed, 
                // but SimpleOnGestureListener handles taps.
            }
            true
        }
    }

    private fun setupPlaybackSeekBar() {
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
    }

    private fun setupVolumeSeekBar() {
        val currentVolume = psv.preferredVolume() ?: audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        psv.rememberSystemVolume(currentVolume)

        binding.playerUI.seekBar.max = maxVolume
        binding.playerUI.seekBar.progress = currentVolume.coerceIn(0, maxVolume)
        psv.rememberUserVolume(binding.playerUI.seekBar.progress)

        binding.playerUI.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val clamped = progress.coerceIn(0, maxVolume)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
                        psv.rememberUserVolume(clamped)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )
    }

    private fun setupBrightnessSeekBar() {
        val currentBrightness = psv.preferredBrightness() ?: getCurrentBrightnessProgress()

        psv.rememberSystemBrightness(getCurrentBrightnessProgress())

        binding.playerUI.seekBar3.max = 100
        binding.playerUI.seekBar3.progress = currentBrightness.coerceIn(0, 100)
        psv.rememberUserBrightness(binding.playerUI.seekBar3.progress)
        applyWindowBrightness(binding.playerUI.seekBar3.progress)

        binding.playerUI.seekBar3.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val clamped = progress.coerceIn(0, 100)
                        psv.rememberUserBrightness(clamped)
                        applyWindowBrightness(clamped)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )
    }

    private fun getCurrentBrightnessProgress(): Int {
        val systemBrightness = Settings.System.getInt(
            activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            128
        )
        return ((systemBrightness / 255f) * 100).toInt().coerceIn(0, 100)
    }

    private fun applyWindowBrightness(progress: Int) {
        val params = activity.window.attributes
        params.screenBrightness = progress.coerceIn(0, 100) / 100f
        activity.window.attributes = params
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

        psv.isLoading.observe(lifecycleOwner) {show->
            binding.loadingIndicator.visibility =
                if (show) View.VISIBLE else View.GONE
        }
    }

    private fun toggleControls() {
        val current = psv.showControls.value ?: true
        psv.showControls.postValue(!current)
    }
}