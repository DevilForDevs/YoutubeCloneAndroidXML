package com.ranjan.expertclient.utils

fun extractVideoId(ytUrl: String): String? {
    val regex = Regex(
        """.*(?:(?:youtu\.be/|v/|vi/|u/\w/|embed/|shorts/|live/)|(?:(?:watch)?\?v(?:i)?=|\&v(?:i)?=))([^#&?]*).*"""
    )

    val match = regex.matchEntire(ytUrl)
    return match?.groupValues?.get(1)
}