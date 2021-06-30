package com.loan555.musicplayer.model

import android.graphics.Bitmap
import java.io.Serializable

data class SongCustom(
    val id: String,// nếu lưu trong local thì lúc dùng sẽ convert ra long( để lấy thumbai, và
    val name: String,
    val artists: String,// nguowif bieeu dieexn
    val duration: Int,
    val size: Int,
    val title: String,
    val albums: String,
    val bitmap: Bitmap?,
    val isLocal: Boolean,// online hay offline
    val linkUri: String,// để chơi nhạc
) : Serializable {
    fun timeToString(): String {
        val sumSeconds = duration / 1000
        val hours = sumSeconds / 3600
        val minute = sumSeconds % 3600 / 60
        val seconds = sumSeconds % 60
        val hString = if (hours < 10) "0$hours" else "$hours"
        val mString = if (minute < 10) "0$minute" else "$minute"
        val sString = if (seconds < 10) "0$seconds" else "$seconds"
        return if (hours == 0) "$mString:$sString" else "$hString:$mString:$sString"
    }
}
