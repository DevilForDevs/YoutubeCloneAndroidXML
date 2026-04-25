package com.ranjan.expertclient.models

data class UrlInfo(
    val contentLength: Long,
    val mimeType: String?,
    val fileName: String?,
    val acceptRanges: Boolean
)
