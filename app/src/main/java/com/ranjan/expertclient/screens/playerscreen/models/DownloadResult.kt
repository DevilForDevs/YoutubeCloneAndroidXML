package com.ranjan.expertclient.screens.playerscreen.models

sealed class DownloadResult {
    object Success : DownloadResult()
    object ResumeNotSupported : DownloadResult()
    object Cancelled : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}