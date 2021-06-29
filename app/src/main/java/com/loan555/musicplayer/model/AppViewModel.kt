package com.loan555.musicplayer.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.R

class AppViewModel : ViewModel() {
    /** ------------------ home fragmentViewModel ----------------------------*/
    val imgDefault = R.drawable.musical_note_icon

    private val listSong = ArrayList<SongCustom>()
    val _mListSongLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listSong
    }

    val mListSongLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongLiveData

    fun addData(song: SongCustom) {
        listSong.add(song)
        _mListSongLiveData.apply {
            value = listSong
        }
        Log.d(MY_TAG,"add $song")
    }

    /** ------------------ dashboardViewModel ----------------------------*/

    var _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    /** ------------------ notificationViewModel ----------------------------*/

    val _text2 = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text2: LiveData<String> = _text2

    /**
     *
     */
    fun getLoad(str: String) {
        _text.value = str
    }
}

/**
Một số chức năng cụ thể mà ViewModel có thể thực hiện :

Truy suất và thông báo hiển thị dữ liệu cho view.

Xử lý visibility của View

Xác thực dữ liệu đầu vào

… ViewModel chỉ nên biết về application context. Chúng ta chỉ nên thực hiện một số hành động liên quan đến context tại ViewModel như sau :

Start a service

Bind to a service

Gửi một broadcast

Đăng ký broadcast receiver

Load resource values

ViewModel không nên:

Hiện thị một dialog

Start activity

Inflate layout*/