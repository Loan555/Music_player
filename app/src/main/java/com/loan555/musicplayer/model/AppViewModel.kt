package com.loan555.musicplayer.model

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loan555.musicplayer.*

class AppViewModel : ViewModel() {

    /**
     * activity
     */

    private val _itemPlayingImg = MutableLiveData<Bitmap>().apply {
        value = null
    }
    private val _title = MutableLiveData<String>().apply {
        value = ""
    }
    private val _artist = MutableLiveData<String>().apply {
        value = ""
    }
    private val _isPlaying = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val _isVisible = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val _songPos = MutableLiveData<Int>().apply {
        value = -1
    }
    private val _listPos = MutableLiveData<Int>().apply {
        value = PLAYLIST_NOTHING
    }
    private val _chartLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val _homeLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val _searchLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val _pageLoader = MutableLiveData<Int>().apply {
        value = -1
    }
    private var _songDownload = MutableLiveData<SongCustom>().apply {
        value = null
    }
    val itemPlayingImg: LiveData<Bitmap> = _itemPlayingImg
    val title: LiveData<String> = _title
    val artist: LiveData<String> = _artist
    val isPlaying: LiveData<Boolean> = _isPlaying
    val isVisible: LiveData<Boolean> = _isVisible
    val songPos: LiveData<Int> = _songPos
    val listPos: LiveData<Int> = _listPos
    val chartLoading: LiveData<Boolean> = _chartLoading
    val homeLoading: LiveData<Boolean> = _homeLoading
    val searchLoading: LiveData<Boolean> = _searchLoading
    val pageLoader: LiveData<Int> = _pageLoader
    val songDownload : LiveData<SongCustom> = _songDownload

    fun sentDownLoad(item: SongCustom){
        _songDownload.value = item
    }

    fun getLoading(pageNumber: Int){
        _pageLoader.value = pageNumber
    }

    fun setLoading(isLoading: Boolean, playList: Int) {
        when (playList) {
            PLAYLIST_CHART -> {
                _chartLoading.value = isLoading
            }
            PLAYLIST_SEARCH -> {
                _searchLoading.value = isLoading
            }
            PLAYLIST_STORAGE -> {
                _homeLoading.value = isLoading
            }
        }
    }

    fun playSong(sP: Int, lP: Int) {
        if (_listPos.value != lP)
            _listPos.value = lP
        _songPos.value = sP
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
        _listPos.value = PLAYLIST_NOTHING
        _songPos.value = -1
    }

    private fun handPlay() {
        _isVisible.value = true
    }

    /**
     * home fragmentViewModel
     */
    private val listTemp = ArrayList<SongCustom>()

    private val _mListSongLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    private val _mListSongChartLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    private val _mListSongSearchLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    val mListSongLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongLiveData
    val mListSongChartLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongChartLiveData
    val mListSongSearchLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongSearchLiveData

    fun readData(songs: ArrayList<SongCustom>, playListID: Int) {
        when (playListID) {
            PLAYLIST_STORAGE -> {
                _mListSongLiveData.apply {
                    value = songs
                }
            }
            PLAYLIST_CHART -> {
                _mListSongChartLiveData.apply {
                    value = songs
                }
            }
            PLAYLIST_SEARCH -> {
                _mListSongSearchLiveData.apply {
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

    private val _textSearch = MutableLiveData<String>().apply {
        value = ""
    }
    val textSearch: LiveData<String> = _textSearch

    /**
     *
     */
    fun getLoad(str: String) {
        _textSearch.value = str
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