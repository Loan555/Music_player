package com.loan555.musicplayer.model

import com.loan555.musicplayer.service.ApiChartService
import com.loan555.musicplayer.service.ApiSearchService
import com.loan555.musicplayer.service.ApiSongDataService

class AppModel {

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
Model chứa phần data được lấy từ nhiều nguồn khác nhau, ví dụ như:

• REST API

• Realm db

• SQLite db

• Handles broadcast

• Shared Preferences

• Firebase*/
