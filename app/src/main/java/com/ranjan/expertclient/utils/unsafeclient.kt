package com.ranjan.expertclient.utils

import android.os.Build
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun getOkHttpClient(): OkHttpClient {
    val builder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Safe default (no SSL bypass)
        return builder.build()
    }

    try {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        builder.sslSocketFactory(
            sslSocketFactory,
            trustAllCerts[0] as X509TrustManager
        ).hostnameVerifier { _, _ -> true }

        return builder.build()

    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}