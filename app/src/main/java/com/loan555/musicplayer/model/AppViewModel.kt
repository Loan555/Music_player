package com.loan555.musicplayer.model

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.PLAYLIST_CHART
import com.loan555.musicplayer.PLAYLIST_STORAGE
import com.loan555.musicplayer.R

class AppViewModel : ViewModel() {
    /**
     * activity
     */
    private var _itemPlayingImg = MutableLiveData<Bitmap>().apply {
        value = null
    }
    private var _title = MutableLiveData<String>().apply {
        value = ""
    }
    private var _artist = MutableLiveData<String>().apply {
        value = ""
    }
    private var _isPlaying = MutableLiveData<Boolean>().apply {
        value = false
    }
    private var _isVisible = MutableLiveData<Boolean>().apply {
        value = false
    }
    private var _songPos = MutableLiveData<Int>().apply {
        value = -1
    }
    private var _listPos = MutableLiveData<Int>().apply {
        value = -1
    }
    val itemPlayingImg: LiveData<Bitmap> = _itemPlayingImg
    val title: LiveData<String> = _title
    val artist: LiveData<String> = _artist
    val isPlaying: LiveData<Boolean> = _isPlaying
    val isVisible: LiveData<Boolean> = _isVisible
    val songPos: LiveData<Int> = _songPos
    val listPos: LiveData<Int> = _listPos

    fun playSong(sP: Int, lP: Int) {
        _songPos.value = sP
        _listPos.value = lP
    }

    fun initItemPlaying(img: Bitmap?, title: String, artist: String, isPlaying: Boolean) {
        handPlay()
        _itemPlayingImg.value = img
        _title.value = title
        _artist.value = artist
        _isPlaying.value = isPlaying
    }

    fun handPause() {
        _isPlaying.value = false
    }

    fun handResume() {
        _isPlaying.value = true
    }

    fun handStop() {
        _isVisible.value = false
    }

    fun handPlay() {
        _isVisible.value = true
    }

    /**
     * home fragmentViewModel
     */
    val imgDefault = R.drawable.musical_note_icon
    private val listTemp = ArrayList<SongCustom>()

    private val _mListSongLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    private val _mListSongChartLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    val mListSongLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongLiveData
    val mListSongChartLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongChartLiveData

    fun addData(song: SongCustom, playListID: Int) {
        when (playListID) {
            PLAYLIST_STORAGE -> {
                listTemp.add(song)
                _mListSongLiveData.apply {
                    value = listTemp
                }
            }
        }
    }

    fun readData(songs: ArrayList<SongCustom>, playListID: Int) {
        when (playListID) {
            PLAYLIST_STORAGE -> {
                _mListSongLiveData.apply {
                    value = songs
                }
            }
            PLAYLIST_CHART ->{
                _mListSongChartLiveData.apply {
                    value = songs
                }
            }
        }
    }

    /**
     * dashboardViewModel
     */

    var _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    /**
     * notificationViewModel
     */

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