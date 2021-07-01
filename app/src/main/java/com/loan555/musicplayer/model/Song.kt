package com.loan555.musicplayer.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class DataSongResult(
    val err: Long,
    val msg: String,
    val data: Song,
    val timestamp: Long
) {
    public fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}

data class Song(
    val id: String,
    val name: String,
    val title: String,
    val code: String,

    @SerializedName("content_owner")
    val contentOwner: Long,
    @SerializedName("isoffical")
    val isOfficial: Boolean,
    val isWorldWide: Boolean,

    @SerializedName("playlist_id")
    val playlistID: String,

    val artists: List<ArtistElement>,

    @SerializedName("artists_names")
    val artistsNames: String,
    @SerializedName("total")
    val total: Long,

    val performer: String,
    val type: String,
    val link: String,
    val lyric: String,
    val thumbnail: String,

    @SerializedName("mv_link")
    val mvLink: String,

    val duration: Long,
    val source: Map<String, String>,
    val album: Album,
    val artist: PurpleArtist,
    val ads: Boolean,

    @SerializedName("is_vip")
    val isVip: Boolean,

    val ip: String
)

data class Album(
    val id: String,
    val link: String,
    val title: String,
    val name: String,
    @SerializedName("isoffical")
    val isOfficial: Boolean,

    @SerializedName("artists_names")
    val artistsNames: String,

    val artists: List<ArtistElement>,
    val thumbnail: String,

    @SerializedName("thumbnail_medium")
    val thumbnailMedium: String
)

data class ArtistElement(
    val name: String,
    val link: String
)

data class PurpleArtist(
    val id: String,
    val name: String,
    val link: String,
    val cover: String,
    val thumbnail: String
)