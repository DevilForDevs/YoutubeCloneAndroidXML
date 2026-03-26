package com.ranjan.expertclient.screens.browserscreen

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.BrowserScreenBinding
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.getValue

class BrowserScreen : Fragment() {
    private lateinit var binding: BrowserScreenBinding
    private val viewModel by activityViewModels<Store>()
    private val chunkMap = mutableMapOf<String, MutableList<String>>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= BrowserScreenBinding.inflate(inflater,container,false)
        return binding.root
    }

    fun goToBottomScreen(){
        viewLifecycleOwner.lifecycleScope.launch {
            findNavController().navigate(R.id.action_browserScreen_to_bottomNavScreen)
        }

    }

    fun onWebviewMessage(message: String) {

       viewModel.handleWebFeed(message,::goToBottomScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        WebView.setWebContentsDebuggingEnabled(true)

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowContentAccess = true


            // 🔥 cookies fix
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            addJavascriptInterface(
                WebAppInterface(::onWebviewMessage,chunkMap),
                "Android"
            )

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.evaluateJavascript(combinedJsCode, null)
                }
            }

            loadUrl("https://www.youtube.com")
        }

    }

}

