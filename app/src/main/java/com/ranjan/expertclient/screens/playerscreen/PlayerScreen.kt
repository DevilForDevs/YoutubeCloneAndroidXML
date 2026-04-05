package com.ranjan.expertclient.screens.playerscreen

import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.utils.formatTime
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetailsAdapter
import com.ranjan.expertclient.screens.playerscreen.widgets.videodetails.VideoDetailsHolder

class PlayerScreen : Fragment() {

    private var player: ExoPlayer?=null
    private lateinit var binding: PlayerScreenBinding

    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val viewModel by activityViewModels<Store>()
    private val psv by activityViewModels<PlayerScreenViewModel>()
    lateinit var dataSourceFactory: DefaultDataSource.Factory
    val videosDetailsAdapter= VideoDetailsAdapter()
    val videosAdapter = VideosColumnAdapter(::onItemClick)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlayerScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        dataSourceFactory = DefaultDataSource.Factory(requireContext())
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player=player
        psv.loadVideo(player!!,sharedViewModel.selectedVideo.value,dataSourceFactory,viewModel.visitorId?:"")
        binding.playerUI.imageView17.setOnClickListener {
            ::showResolutionsModal
        }
        psv.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.loadingIndicator.visibility =
                if (loading) View.VISIBLE else View.GONE
        }

        psv.totalDuration.observe(viewLifecycleOwner){totalDuration->
            if (totalDuration!=null){
                binding.playerUI.textView13.text=formatTime(totalDuration)
            }
            if (totalDuration!=null){
                binding.playerUI.linearLayout.max = totalDuration.toInt()
            }
            binding.playerUI.imageView14.setImageResource(R.drawable.pause)


        }
        psv.durationProgress.observe(viewLifecycleOwner){progressDuration->

            if (progressDuration!=null){
                binding.playerUI.textView12.text=formatTime(progressDuration)
            }

            if (progressDuration!=null){
                binding.playerUI.linearLayout.progress= progressDuration.toInt()
            }

        }
        binding.playerUI.imageView14.setOnClickListener {
            if (player!=null){
                psv.togglePlayBack(player!!)
            }

        }
        psv.isPaused.observe(viewLifecycleOwner){isPaused->
            if (isPaused){
                binding.playerUI.imageView14.setImageResource(R.drawable.play)
            }else{
                binding.playerUI.imageView14.setImageResource(R.drawable.pause)
            }
        }
        psv.showControls.observe(viewLifecycleOwner) { show ->
            if (show) {
                binding.playerUI.root.visibility = View.VISIBLE
            } else {
                binding.playerUI.root.visibility = View.GONE
            }
        }

        binding.playerView.setOnClickListener {
            psv.toggleControls()
        }
        binding.playerUI.imageView13.setOnClickListener {
            psv.toggleFullScreen()
        }
        psv.isFullScreen.observe(viewLifecycleOwner) { isFull ->
            val activity = requireActivity()
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)

            if (isFull) {

                activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                binding.playerContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                binding.playerContainer.requestLayout()

                val params = binding.playerUI.linearLayout.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = 20.dp
                binding.playerUI.linearLayout.layoutParams = params
                binding.playerUI.imageView13.setImageResource(R.drawable.baseline_fullscreen_exit_24)


            } else {
                activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                controller.show(WindowInsetsCompat.Type.systemBars())
                binding.playerContainer.layoutParams.height = 250.dp
                binding.playerContainer.requestLayout()

                val params = binding.playerUI.linearLayout.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = 0.dp
                binding.playerUI.linearLayout.layoutParams = params
                binding.playerUI.imageView13.setImageResource(R.drawable.baseline_fullscreen_24)


            }

        }

        val concatAdapter = ConcatAdapter(
            videosDetailsAdapter,
            videosAdapter

        )
        binding.suggestionRecycler.apply {
            adapter=concatAdapter
            layoutManager =LinearLayoutManager(requireContext())
        }
        psv.suggestionsList.observe(viewLifecycleOwner){videos->
            videosAdapter.submitList(videos)
        }
        psv.videoDetails.observe(viewLifecycleOwner){details ->
            val holder = binding.suggestionRecycler.findViewHolderForAdapterPosition(0)
            if (holder is VideoDetailsHolder) {
                if (details!=null){
                    holder.bind(details) // manually update the view
                }

            }
        }

        binding.suggestionRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // trigger when last item is visible
                if (lastVisibleItem >= totalItemCount - 2) {
                    psv.loadMoreSuggestions(viewModel.visitorId)
                }
            }
        })

    }
    fun onItemClick(item: VideoItem){
        if (player!=null){
            psv.loadVideo(player!!,item,dataSourceFactory,viewModel.visitorId?:"")
        }

    }


    fun showResolutionsModal(){

    }
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    override fun onPause() {
        super.onPause()
        player?.pause()
    }

}


