package com.ranjan.expertclient.utils

import android.os.Environment
import android.os.StatFs
import java.io.File
import kotlin.math.roundToInt

fun convertBytes(sizeInBytes: Long): String {
    val KB = 1024L
    val MB = KB * 1024
    val GB = MB * 1024

    return when {
        sizeInBytes >= GB -> "${Math.round(sizeInBytes.toDouble() / GB)} GB"
        sizeInBytes >= MB -> "${Math.round(sizeInBytes.toDouble() / MB)} MB"
        sizeInBytes >= KB -> "${Math.round(sizeInBytes.toDouble() / KB)} KB"
        else -> "$sizeInBytes Bytes"
    }
}

fun txt2filename(txt: String): String {
    val specialCharacters = listOf(
        "@", "#", "$", "*", "&", "<", ">", "/", "\\b", "|", "?", "CON", "PRN", "AUX", "NUL",
        "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT0",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", ":", "\"", "'"
    )

    var normalString = txt
    for (sc in specialCharacters) {
        normalString = normalString.replace(sc, "")
    }

    return normalString
}

fun getFreeDiskSpace(path: File = Environment.getDataDirectory()): Long {
    val stat = StatFs(path.path)
    val blockSize = stat.blockSizeLong
    val availableBlocks = stat.availableBlocksLong
    return availableBlocks * blockSize
}

fun convertSpeed(bytesPerSec: Long): String {
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        bytesPerSec >= gigabyte -> "${(bytesPerSec / gigabyte).roundToInt()} GB/s"
        bytesPerSec >= megabyte -> "${(bytesPerSec / megabyte).roundToInt()} MB/s"
        bytesPerSec >= kilobyte -> "${(bytesPerSec / kilobyte).roundToInt()} KB/s"
        else -> "$bytesPerSec B/s"
    }
}

