package com.ranjan.expertclient.screens.browserscreen

import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.joinToString
import kotlin.collections.mutableMapOf

class WebAppInterface(
    private val onMessage: (String) -> Unit,
    private val chunkMap: MutableMap<String, MutableList<String>>
) {

    @JavascriptInterface
    fun postMessage(message: String) {
        val json = JSONObject(message)

        val type = json.optString("type")

        // 🔥 Ignore script loaded
        if (type == "SCRIPT_LOADED") return

        // 🔥 Handle chunks
        if (json.has("chunk")) {
            val index = json.optInt("index")
            val total = json.optInt("total")
            val chunk = json.optString("chunk")

            val key = type // e.g., YT_BROWSE

            val list = chunkMap.getOrPut(key) {
                MutableList(total) { "" }
            }

            list[index] = chunk
        }

        // 🔥 Handle DONE
        if (type.endsWith("_DONE")) {
            val baseType = type.removeSuffix("_DONE")

            val chunks = chunkMap[baseType] ?: return

            val fullJsonString = chunks.joinToString("")
            onMessage(fullJsonString)

//            try {
//
//                val finalJson = JSONObject(fullJsonString)
//                if (finalJson.has("url")){
//                    if (finalJson.getString("url").contains("search")){
//                        val responseContext=finalJson.getJSONObject("data")
//                        val visitorId = safeGet(
//                            responseContext,
//                            listOf("responseContext", "webResponseContextExtensionData", "ytConfigData", "visitorData")
//                        )
//                        println(visitorId)
//                        val clientVersion=safeGet(responseContext,
//                            listOf("responseContext","serviceTrackingParams",0,"params",2,"value"))
//                        println(clientVersion)
//                    }
//                }
//            } catch (e: Exception) {
//                println("❌ JSON parse error: ${e.message}")
//            }
            chunkMap.remove(baseType)
        }
    }



}

fun safeGet(
    obj: Any?,
    path: List<Any>,
    defaultVal: Any? = null
): Any? {
    var cur: Any? = obj

    return try {
        for (p in path) {
            if (cur == null) return defaultVal

            cur = when (p) {
                is String -> {
                    if (cur is JSONObject) cur.opt(p) else return defaultVal
                }
                is Int -> {
                    if (cur is JSONArray) {
                        val index = if (p < 0) cur.length() + p else p
                        if (index in 0 until cur.length()) cur.opt(index) else return defaultVal
                    } else return defaultVal
                }
                else -> return defaultVal
            }
        }
        cur ?: defaultVal
    } catch (e: Exception) {
        defaultVal
    }
}

fun convertMutableList(iterator: Iterator<String>): MutableList<String> {
    val list = mutableListOf<String>()

    while (iterator.hasNext()) {
        list.add(iterator.next())
    }

    return list
}

val combinedJsCode = """
(function () {
  const CHUNK_SIZE = 50000;

  function send(msg) {
    if (window.Android && window.Android.postMessage) {
      window.Android.postMessage(JSON.stringify(msg));
    }
  }

  function postInChunks(type, payload) {
    try {
      const jsonString = JSON.stringify(payload);
      const total = Math.ceil(jsonString.length / CHUNK_SIZE);

      for (let i = 0; i < total; i++) {
        const chunk = jsonString.slice(i * CHUNK_SIZE, (i + 1) * CHUNK_SIZE);
        send({
          type,
          index: i,
          total,
          chunk,
          url: location.href,
        });
      }

      send({
        type: type + "_DONE",
        total,
        url: location.href,
      });

    } catch (error) {
      send({
        type: "ERROR",
        error: error.message,
      });
    }
  }

  // 🔥 FETCH INTERCEPTOR (early)
  (function () {
    const originalFetch = window.fetch;

    window.fetch = function (...args) {
      return originalFetch.apply(this, args).then((response) => {
        try {
          const url = response.url || "";

          if (url.includes("/youtubei/v1/")) {
            response.clone().json().then((data) => {

              let type = "YT_API";

              if (url.includes("browse")) type = "YT_BROWSE";
              if (url.includes("search")) type = "YT_SEARCH";
              if (url.includes("next")) type = "YT_NEXT";

              postInChunks(type, {
                url,
                data
              });

            }).catch(() => {});
          }

        } catch (e) {}

        return response;
      });
    };
  })();

  // 🔥 XHR INTERCEPTOR (VERY IMPORTANT)
  (function () {
    const origOpen = XMLHttpRequest.prototype.open;
    const origSend = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.open = function (method, url) {
      this._url = url;
      return origOpen.apply(this, arguments);
    };

    XMLHttpRequest.prototype.send = function () {
      this.addEventListener("load", function () {
        try {
          if (this._url && this._url.includes("/youtubei/v1/")) {
            const data = JSON.parse(this.responseText);

            let type = "YT_API";

            if (this._url.includes("browse")) type = "YT_BROWSE";
            if (this._url.includes("search")) type = "YT_SEARCH";
            if (this._url.includes("next")) type = "YT_NEXT";

            postInChunks(type, {
              url: this._url,
              data
            });
          }
        } catch (e) {}
      });

      return origSend.apply(this, arguments);
    };
  })();

  // 🔥 INITIAL DATA FALLBACK (sometimes exists)
  function captureInitialData() {
    try {
      const ytData =
        window.ytInitialData ||
        window.ytInitialPlayerResponse ||
        window.ytplayer;

      if (ytData) {
        postInChunks("YT_INITIAL_DATA", {
          data: ytData,
          url: location.href,
          title: document.title
        });
      }
    } catch (e) {}
  }

  // 🔥 URL CHANGE DETECTION
  let lastUrl = location.href;

  new MutationObserver(() => {
    if (location.href !== lastUrl) {
      lastUrl = location.href;

      setTimeout(() => {
        captureInitialData();
      }, 1000);
    }
  }).observe(document, { subtree: true, childList: true });

  // 🔥 START ASAP
  send({ type: "SCRIPT_LOADED" });

  // Try multiple times (important for homepage)
  let tries = 0;
  const interval = setInterval(() => {
    captureInitialData();
    tries++;
    if (tries > 10) clearInterval(interval);
  }, 500);

})();
"""