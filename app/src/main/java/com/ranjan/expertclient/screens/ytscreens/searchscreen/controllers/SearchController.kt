package com.ranjan.expertclient.screens.ytscreens.searchscreen.controllers

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.YtSearchScreenBinding
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.ytscreens.searchscreen.SearchScreenViewModel

class SearchController(
    private val binding: YtSearchScreenBinding,
    private val back:()-> Unit,
    private val adapter: VideosColumnAdapter,
    private val ssvm: SearchScreenViewModel,
    private val lifecycleOwner: LifecycleOwner

){
    init {
        setupSearchField()
    }
    @SuppressLint("ClickableViewAccessibility")
    fun setupSearchField(){
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
            back()
        }

        binding.searchItems.layoutManager =
            LinearLayoutManager(binding.root.context)
        binding.searchItems.adapter=adapter

        binding.editTextText.setOnEditorActionListener { v, actionId, event ->

            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editTextText.text.toString()
                ssvm.getInitialResult(query)
                binding.editTextText.text.clear()
                true
            } else {
                false
            }
        }
        ssvm._isLoading.observe(lifecycleOwner){isLoading->
            binding.progressBar2.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
        ssvm.search_items.observe(lifecycleOwner){
            adapter.submitList(it)
        }
            binding.searchItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    if (lm.findLastVisibleItemPosition() >= lm.itemCount - 2) {
                        ssvm.loadMore()
                    }
                }
            })
    }


}