package com.loan555.musicplayer.model

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.PLAYLIST_CHART
import com.loan555.musicplayer.service.ApiChartService
import com.loan555.musicplayer.service.ApiSearchService
import com.loan555.musicplayer.service.ApiSongDataService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

class SongList() {
    var playList: ArrayList<SongCustom> = ArrayList()
    var listID = -1

    fun getListFromStorage(context: Context): Boolean {
        var resultOK = false
        val collection: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val protection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = null
        val selectionArgs = null
        val sortOder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = context.applicationContext.contentResolver.query(
            collection,
            protection,
            selection,
            selectionArgs,
            sortOder
        )
        val newSongs = ArrayList<SongCustom>()
        try {
            query?.use { cursor ->
                newSongs.clear()
                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                while (cursor.moveToNext()) {
                    // Get values of columns for a given audio.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val artists = cursor.getString(artistsColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)
                    val title = cursor.getString(titleColumn)
                    val albums = cursor.getString(albumsColumn)

                    //load content Uri
                    val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    // Load thumbnail of a specific media item.
                    var thumbnail: Bitmap? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val thumb =
                                context.applicationContext.contentResolver.loadThumbnail(
                                    contentUri, Size(640, 480), null
                                )
                            thumbnail = thumb
                        } catch (e: IOException) {
                            Log.e(MY_TAG, "can't find bitmap: ${e.message}")
                        }
                    }
                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    val newSong = SongCustom(
                        id.toString(),
                        name,
                        artists,
                        duration,
                        size,
                        title,
                        albums,
                        thumbnail,
                        true,
                        contentUri.toString()
                    )
                    newSongs.add(newSong)
                }
            }
            playList = newSongs
            listID = 0
            resultOK = true
        } catch (e: Exception) {
            Log.e(MY_TAG, "error getListFromStorage: ${e.message}")
        }
        return resultOK
    }

    fun getListFromApiChart(context: Context): Boolean {
        Log.d(MY_TAG, "getCurrentChartData")
        var resultOK = false
        val call = serviceApiGetChart.getCurrentData(songId, videoId, albumId, chart, time)
        call.enqueue(object : Callback<DataChartResult> {
            override fun onResponse(
                call: Call<DataChartResult>,
                response: Response<DataChartResult>
            ) {
                if (response.code() == 200) {
                    Log.d(MY_TAG,"response.code() == 200")
                    val dataResponse = response.body()!!
                    val listChart = dataResponse.data
                    listChart.song.forEach {
                        // Load thumbnail of a specific media item.
                        var thumbnail: Bitmap? = null
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                val thumb =
                                    context.applicationContext.contentResolver.loadThumbnail(
                                        Uri.parse(it.thumbnail), Size(640, 480), null
                                    )
                                thumbnail = thumb
                            } catch (e: IOException) {
                            }
                        }
                        playList.add(
                            SongCustom(
                                it.id,
                                it.name,
                                it.artistsNames,
                                it.duration.toInt(),
                                280,
                                it.title,
                                "it.album.toString()",
                                thumbnail,
                                false,
                                "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320"
                            )
                        )
                    }
                    listID = PLAYLIST_CHART
                    resultOK = true
                    Log.d(MY_TAG, "loadDataChartList success ")
                } else Log.e("aaa", "response.code() = ${response.code()}")
            }

            override fun onFailure(call: Call<DataChartResult>, t: Throwable) {
                Log.e(MY_TAG, "error getCurrentChartData ${t.message}")
            }
        })
        return resultOK
    }

    companion object {
        //http://mp3.zing.vn/xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1
        val serviceApiGetChart by lazy { ApiChartService.create() }
        var songId = 0
        var videoId = 0
        var albumId = 0
        var chart = "song"
        var time = -1

        ////http://mp3.zing.vn/xhr/media/get-source?type=audio&key=kmJHTZHNCVaSmSuymyFHLH
        val serviceApiGetSong by lazy { ApiSongDataService.create() }
        var type = "audio"
        var key = "ZHJmyZkHLzcbAEgyGTbnkGyLhbzchkRsm"// key laf code

        ////http://ac.mp3.zing.vn/complete?type=artist,song,key,code&num=500&query=Anh Thế Giới Và Em
        val apiSearchService by lazy { ApiSearchService.create() }
        var typeSearch = "artist,song,key,code"
        var num = 500.toLong()
        var query = "Anh Thế Giới Và Em"
    }
}

/**
 * các play list
 */
class PlayLists() {
    private val playLists = ArrayList<SongList>()

}