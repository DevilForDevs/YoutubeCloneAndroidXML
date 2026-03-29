package com.ranjan.expertclient.screens.searchscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.YtSearchScreenBinding
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter

class SearchScreen : Fragment() {

    private lateinit var binding: YtSearchScreenBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = YtSearchScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        binding.editTextText.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                binding.editTextText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                binding.editTextText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_close_24, 0)
            }
        }

        binding.editTextText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = binding.editTextText.compoundDrawables[drawableEnd]

                if (drawable != null) {
                    if (event.rawX >= (binding.editTextText.right - drawable.bounds.width())) {
                        binding.editTextText.text.clear()
                        // IMPORTANT: accessibility fix
                        v.performClick()

                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
        binding.imageView7.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.searchItems.layoutManager =
            LinearLayoutManager(binding.root.context)





    }

}