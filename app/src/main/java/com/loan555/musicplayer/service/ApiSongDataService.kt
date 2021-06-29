package com.loan555.musicplayer.service

import com.google.gson.GsonBuilder
import com.loan555.musicplayer.model.DataChartResult
import com.loan555.musicplayer.model.DataSongResult
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiSongDataService {
    //http://mp3.zing.vn/xhr/media/get-source?type=audio&key=kmJHTZHNCVaSmSuymyFHLH
    @GET("xhr/media/get-source")
    fun getCurrentData(
        @Query("type") type: String,
        @Query("key") key: String
    ): Call<DataSongResult>

    companion object {
        private const val BaseUrl = "http://mp3.zing.vn/"
        fun create(): ApiSongDataService {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ApiSongDataService::class.java)
        }
    }
}