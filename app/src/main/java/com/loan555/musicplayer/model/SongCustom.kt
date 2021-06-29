package com.loan555.musicplayer.model

import java.io.Serializable

data class SongCustom(
    val id: String,// nếu lưu trong local thì lúc dùng sẽ convert ra long( để lấy thumbai, và
    val name: String,
    val artists: String,// nguowif bieeu dieexn
    val duration: Int,
    val size: Int,
    val title: String,
    val albums: String,
    val isLocal: Boolean,// online hay offline
    val linkUri: String,// để chơi nhạc
) : Serializable
