package com.ranjan.expertclient.screens.playerscreen.controllers

import android.os.Environment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.DialogResolutionsBinding
import com.ranjan.expertclient.screens.playerscreen.MoviesViewModel
import com.ranjan.expertclient.screens.playerscreen.widgets.download.DownloadsAdapter
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.utils.convertBytes
import com.ranjan.expertclient.utils.getFreeDiskSpace

class DownloadAndPlay(
    private val fragment: Fragment,
    private val onActionClick: (DownloadItem) -> Unit,
    private val viewModel: MoviesViewModel,
    private val play: (item: DownloadItem) -> Unit
) {
    private var dialog: BottomSheetDialog? = null
    private val adapter = DownloadsAdapter(onActionClick, play)

    init {
        // Observe once for the lifetime of the Fragment's view
        viewModel.downloads.observe(fragment.viewLifecycleOwner) { items ->
            println(items)
            adapter.submitList(items?.toList())
            // If dialog is showing, the adapter will update automatically
        }
    }

    fun show() {
        if (dialog?.isShowing == true) return
        val freeSpace = getFreeDiskSpace(Environment.getDataDirectory())


        val context = fragment.requireContext()
        val binding = DialogResolutionsBinding.inflate(fragment.layoutInflater)
        
        binding.resolutionRecycler.apply {
            adapter = this@DownloadAndPlay.adapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Show free space using string resource (Works Offline)
        binding.freeSpaceText.text = context.getString(R.string.free_space, convertBytes(freeSpace))

        // Navigate to Library/Downloads when clicked
        binding.goToDownloads.setOnClickListener {
            fragment.findNavController().navigate(R.id.bottomNavScreen)
            dismiss()
        }

        dialog = BottomSheetDialog(context).apply {
            setContentView(binding.root)
            setOnDismissListener { 
                dialog = null 
            }
            show()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}
