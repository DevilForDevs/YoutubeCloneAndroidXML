package com.ranjan.expertclient.screens.browserscreen

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.BrowserScreenBinding
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import kotlinx.coroutines.launch

class BrowserScreen : Fragment() {
    private lateinit var binding: BrowserScreenBinding
    private val viewModel by activityViewModels<Store>()
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val chunkMap = mutableMapOf<String, MutableList<String>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BrowserScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { rootView, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            rootView.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldNavigateBack ->
            if (shouldNavigateBack == true) {
                findNavController().popBackStack()
                viewModel.onBackNavigationHandled()
            }
        }

        val choosingDomainTitle = viewModel.chooseDomain.value
        val isChoosingDomain = !choosingDomainTitle.isNullOrBlank()
        val normalStartUrl = sharedViewModel.selectedSite.value?.url ?: "https://m.youtube.com/"
        val startUrl = if (isChoosingDomain) {
            val encodedQuery = Uri.encode(choosingDomainTitle)
            "https://www.google.com/search?q=$encodedQuery"
        } else {
            normalStartUrl
        }

        binding.btnChooseDomain.visibility = if (isChoosingDomain) View.VISIBLE else View.GONE
        binding.btnChooseDomain.setOnClickListener {
            viewModel.chooseCurrentDomain(requireContext())
        }

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowContentAccess = true

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            // Only attach scraping bridge in normal feed mode.
            if (!isChoosingDomain) {
                addJavascriptInterface(WebAppInterface(::onWebviewMessage, chunkMap), "Android")
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    viewModel.updateCurrentUrl(url)
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.updateCurrentUrl(url)
                    if (!isChoosingDomain) {
                        view?.evaluateJavascript(combinedJsCode, null)
                    }
                }
            }

            loadUrl(startUrl)
        }
    }

    fun goToBottomScreen() {
        viewLifecycleOwner.lifecycleScope.launch {
            findNavController().navigate(R.id.action_browserScreen_to_bottomNavScreen)
        }
    }

    fun onWebviewMessage(message: String) {
        if (viewModel.chooseDomain.value != null) return
        viewModel.handleWebFeed(message, ::goToBottomScreen)
    }
}
