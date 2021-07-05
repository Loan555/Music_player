package com.loan555.musicplayer.model

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.loan555.musicplayer.*

class AppViewModel : ViewModel() {

    /**
     * activity
     */

    private val _itemPlayingImg = MutableLiveData<Bitmap>().apply {
        value = null
    }
    private val _isStop = MutableLiveData<Boolean>().apply {
        value = true
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
    private var _statePlay = MutableLiveData<Int>().apply {
        value = 0
    }
    private var _actionMusic = MutableLiveData<Int>().apply {
        value = 0//=0 thi ko co action nao
    }
    private var _seekbarPos = MutableLiveData<Int>().apply {
        value = 0//=0 thi ko co action nao
    }
    private var _seekbarMax = MutableLiveData<Int>().apply {
        value = 0//=0 thi ko co action nao
    }
    private var _btnLoadClick = MutableLiveData<Int>().apply {
        value = 0//=0 thi ko co action nao
    }
    private var _optionClick = MutableLiveData<Int>().apply {
        value = 0//=0 thi ko co action nao
    }
    private var _btnLikeListClick = MutableLiveData<Int>().apply {
        value = 0//=0 thi ko co action nao
    }
    private var _likeSong = MutableLiveData<SongCustom>().apply {
        value = null//=0 thi ko co action nao
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
    val songDownload: LiveData<SongCustom> = _songDownload
    val statePlay: LiveData<Int> = _statePlay
    val actionMusic: LiveData<Int> = _actionMusic
    val seekbarPos: LiveData<Int> = _seekbarPos
    val seekbarMax: LiveData<Int> = _seekbarMax
    val btnLoadClick: LiveData<Int> = _btnLoadClick
    val isStop: LiveData<Boolean> = _isStop
    val optionClick: LiveData<Int> = _optionClick
    val likeSong: LiveData<SongCustom> = _likeSong
    val btnLikeClick: LiveData<Int> = _btnLikeListClick

    fun setBtnLikeClick(){
        _btnLikeListClick.value = 0
    }

    fun setOptionClick(option: Int, item: SongCustom) {
        when (option) {
            R.id.popup_like -> {
                Log.d(MY_TAG,"item = $item")
                _likeSong.value = item
            }
        }
    }

    fun setStopPlayer(isTop: Boolean) {
        _isStop.value = isTop
    }

    fun loadClick() {
        _btnLoadClick.value = _btnLoadClick.value?.plus(1)
    }

    fun seekBarChange(newPos: Int) {
        _seekbarPos.value = newPos
    }

    fun seekBarSer(duration: Int) {
        _seekbarMax.value = duration
    }

    fun sentActionMusic(action: Int) {
        _actionMusic.value = action
    }

    fun setStatePlay(state: Int) {
        _statePlay.value = state % 4
    }

    fun sentDownLoad(item: SongCustom) {
        _songDownload.value = item
    }

    fun getLoading(pageNumber: Int) {
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
            PLAYLIST_STORAGE, PLAYLIST_RELATED -> {
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
        if (img != null) {
            _itemPlayingImg.value = img
        }
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
    private val _mListSongRelateLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    private val _mListSongLikeLiveData = MutableLiveData<ArrayList<SongCustom>>().apply {
        value = listTemp
    }
    val mListSongLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongLiveData
    val mListSongChartLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongChartLiveData
    val mListSongSearchLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongSearchLiveData
    val mListSongRelateLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongRelateLiveData
    val mListSongLikeLiveData: MutableLiveData<ArrayList<SongCustom>> = _mListSongLikeLiveData

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
            PLAYLIST_RELATED -> {
                _mListSongRelateLiveData.apply {
                    value = songs
                }
            }
            PLAYLIST_Like->{
                _mListSongLikeLiveData.apply {
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