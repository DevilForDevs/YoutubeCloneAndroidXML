package com.ranjan.expertclient.screens.playerscreen.controllers

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ranjan.expertclient.databinding.DialogResolutionsBinding
import com.ranjan.expertclient.screens.playerscreen.widgets.resolution.ResolutionAdapter

class ResolutionDialog(private val fragment: Fragment) {

    fun show(
        resolutions: List<String>,
        current: String?,
        onSelect: (String) -> Unit
    ) {
        if (resolutions.isEmpty()) return

        val dialog = BottomSheetDialog(fragment.requireContext())
        val binding = DialogResolutionsBinding.inflate(fragment.layoutInflater)

        val adapter = ResolutionAdapter {
            onSelect(it)
            dialog.dismiss()
        }

        binding.resolutionRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        adapter.submitList(resolutions, current)

        dialog.setContentView(binding.root)
        dialog.show()
    }
}