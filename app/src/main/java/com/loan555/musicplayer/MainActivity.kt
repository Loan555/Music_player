package com.loan555.musicplayer

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.loan555.musicplayer.databinding.ActivityMainBinding
import com.loan555.musicplayer.model.*
import com.loan555.musicplayer.service.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

const val NUM_PAGES = 5
const val MY_TAG = "aaa"
const val STORAGE_REQUEST_CODE = 1

const val PLAYLIST_NOTHING = -1
const val PLAYLIST_STORAGE = 0
const val PLAYLIST_CHART = 1
const val PLAYLIST_SEARCH = 2
const val PLAYLIST_RELATED = 3
const val PLAYLIST_Like = 4

class MainActivity : AppCompatActivity() {
    val dbHelper = SongReaderDbHelper(this)

    //broadcast
    private lateinit var br: BroadcastReceiver

    //permission
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    //view
    private lateinit var mPager: ViewPager
    private lateinit var mainViewModel: AppViewModel
    private lateinit var binding: ActivityMainBinding

    //service
    lateinit var mService: MusicControllerService
    var mBound: Boolean = false

    //data
    private val mPlayListStorage = PlayList()
    private val mPlayListChart = PlayList()
    private val mPlayListSearch = PlayList()
    private val mPlayListRelated = PlayList()
    private val mPlayListLike = PlayList()
    private val mPlayLists = ArrayList<PlayList>()// khoi tao mot danh sacsh cacs playList rong

    /** Defines callbacks for service binding, passed to bindService()  */
    private val conn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.d("aaa", "onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicControllerService.MusicControllerBinder
            mService = binder.getService()
            mBound = true
            if (mService.songs.size > 0) {
                Log.d(
                    MY_TAG,
                    "service is playing ${mService.songs[mService.songPos].toString()}"
                )
                mainViewModel.initItemPlaying(
                    mService.songs[mService.songPos].bitmap,
                    mService.songs[mService.songPos].title,
                    mService.songs[mService.songPos].artists, mService.isPlaying
                )
            }
            mainViewModel.listPos.observe(
                this@MainActivity,
                {// khi list có thay đổ thì phải load lại data vào listPlaying trong service
                    if (mBound) {
                        when (it) {
                            PLAYLIST_STORAGE -> {
                                Log.d(MY_TAG, "click STORANG LIST")
                                mService.songs = mPlayLists[PLAYLIST_STORAGE].playList
                                mService.listPlaying = mPlayLists[PLAYLIST_STORAGE].id
                            }
                            PLAYLIST_CHART -> {
                                Log.d(MY_TAG, "click chart list")
                                mService.songs = mPlayLists[PLAYLIST_CHART].playList
                                mService.listPlaying = mPlayLists[PLAYLIST_CHART].id
                            }
                            PLAYLIST_SEARCH -> {
                                Log.d(MY_TAG, "click search list")
                                mService.songs = mPlayLists[PLAYLIST_SEARCH].playList
                                mService.listPlaying = mPlayLists[PLAYLIST_SEARCH].id
                            }
                            PLAYLIST_RELATED -> {
                                Log.d(MY_TAG, "click relate list")
                                mService.songs = mPlayLists[PLAYLIST_RELATED].playList
                                mService.listPlaying = mPlayLists[PLAYLIST_RELATED].id
                            }
                            PLAYLIST_Like -> {
                                Log.d(MY_TAG, "click like list")
                                mService.songs = mPlayLists[PLAYLIST_Like].playList
                                mService.listPlaying = mPlayLists[PLAYLIST_Like].id
                            }
                        }
                    }
                })
            mainViewModel.songPos.observe(this@MainActivity, {
                Log.d(MY_TAG, "click new posision song")
                if (it >= 0 && it < mService.songs.size)
                    mService.playSong(it)
            })
            mainViewModel.btnLoadClick.observe(this@MainActivity, {
//            if (!mService.songs[mService.songPos].isLocal)
                if (it != 0 && mService.player != null)
                    relatedSong(mService.songIDPlaying)
            })
            mainViewModel.statePlay.observe(this@MainActivity, {
                mService.statePlay = it % 4
            })
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(MY_TAG, "onCreate activity")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this@MainActivity).get(AppViewModel::class.java)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.title_home)

        //viewPager + navigation
        mPager = findViewById(R.id.pager_main)
        val pagerAdapter = MainPagerAdapter(supportFragmentManager)
        mPager.adapter = pagerAdapter
        mPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        nav_view.menu.findItem(R.id.navigation_offline).isChecked = true
                        supportActionBar?.setTitle(R.string.title_home)
                    }
                    1 -> {
                        nav_view.menu.findItem(R.id.navigation_chart).isChecked = true
                        supportActionBar?.setTitle(R.string.title_dashboard)
                    }
                    else -> {
                        nav_view.menu.findItem(R.id.navigation_search).isChecked = true
                        supportActionBar?.setTitle(R.string.title_notifications)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
        nav_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_chart -> {
                    mPager.currentItem = 1
                    true
                }
                R.id.navigation_search -> {
                    mPager.currentItem = 2
                    true
                }
                else -> {
                    mPager.currentItem = 0
                    true
                }
            }
        }

        mainViewModel.itemPlayingImg.observe(this, {
            if (it != null)
                binding.imgSong.setImageBitmap(it)
            else binding.imgSong.setImageResource(R.drawable.musical_note_icon)
        })
        mainViewModel.title.observe(this, {
            binding.songName.text = it
        })
        mainViewModel.artist.observe(this, {
            binding.artistsNames.text = it
        })
        mainViewModel.isPlaying.observe(this, {
            if (it)
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            else binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        })
        mainViewModel.isVisible.observe(this, {
            if (it)
                binding.itemPlaying.visibility = View.VISIBLE
            else binding.itemPlaying.visibility = View.GONE
        })
        mainViewModel.actionMusic.observe(this, {
            when (it) {
                0 -> {
                }
                ACTION_PLAY_PAUSE -> {
                    if (mService.player != null)
                        handBtnPlayPause()
                }
                ACTION_BACK, ACTION_NEXT -> {
                    if (mService.player != null)
                        controlMusic(it)
                }
            }
        })
        mainViewModel.textSearch.observe(this, {
            searchSong(it)
        })
        mainViewModel.pageLoader.observe(this, {
            when (it) {
                0 -> {
                    loadDataStorage()
                }
                1 -> {
                    loadDataDataChart()
                }
                3 -> {
                    mainViewModel.textSearch.value?.let { it1 -> searchSong(it1) }
                }
            }
        })
        mainViewModel.songDownload.observe(this, {
            if (it != null) {
                downLoad(it)
            }
        })
        mainViewModel.likeSong.observe(this, {
            if (it != null) {
                mPlayLists[PLAYLIST_Like].playList.add(it)
                saveInDataBase(it)
                Toast.makeText(this, "add in like list: ${it.title}", Toast.LENGTH_SHORT).show()
            }
        })
        mainViewModel.btnLikeClick.observe(this, {
            Log.d(MY_TAG, "click like list")
            GlobalScope.launch(Dispatchers.Main) {
                val resultOK = async(Dispatchers.IO) {
                    return@async readFromLikeList()
                }
                if (resultOK.await()) {
                    mainViewModel.readData(mPlayLists[PLAYLIST_Like].playList, PLAYLIST_Like)
                    Log.d(MY_TAG, "click like list OK")
                }
            }
        })
        /**
         * Event listener
         */
        binding.btnPlayPause.setOnClickListener {
            handBtnPlayPause()
        }
        binding.btnSkipNext.setOnClickListener {
            controlMusic(ACTION_NEXT)
        }
        binding.btnSkipPrevious.setOnClickListener {
            controlMusic(ACTION_BACK)
        }
        binding.itemPlaying.setOnClickListener {
            binding.pagerMain.currentItem = 3
            if (mService.player != null)
                mainViewModel.setStopPlayer(false)
            else mainViewModel.setStopPlayer(true)
        }
        /**
         * load data
         */
        mPlayLists.add(mPlayListStorage)
        mPlayLists.add(mPlayListChart)
        mPlayLists.add(mPlayListSearch)
        mPlayLists.add(mPlayListRelated)// them 4 play list vao danh sach
        mPlayLists.add(mPlayListLike)
        loadDataStorage()
        loadDataDataChart()
    }

    private fun readFromLikeList(): Boolean {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            SongReaderContract.SongEntry.COLUMN_ID,
            SongReaderContract.SongEntry.COLUMN_NAME,
            SongReaderContract.SongEntry.COLUMN_TITLE,
            SongReaderContract.SongEntry.COLUMN_ARTISTS,
            SongReaderContract.SongEntry.COLUMN_DURATION,
            SongReaderContract.SongEntry.COLUMN_THUMBNAIL,
            SongReaderContract.SongEntry.COLUMN_URL
        )
        val cursor = db.query(
            SongReaderContract.SongEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        mPlayLists[PLAYLIST_Like].id = PLAYLIST_Like
        mPlayLists[PLAYLIST_Like].playList.clear()
        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_ID))
                val name =
                    getString(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_NAME))
                val title =
                    getString(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_TITLE))
                val artist =
                    getString(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_ARTISTS))
                val duration =
                    getInt(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_DURATION))
                val thumbnail =
                    getString(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_THUMBNAIL))
                val url = getString(getColumnIndexOrThrow(SongReaderContract.SongEntry.COLUMN_URL))
                if (thumbnail != null) {
                    val newSong = SongCustom(
                        id,
                        name,
                        artist,
                        duration,
                        0,
                        title,
                        "",
                        null,
                        thumbnail,
                        false,
                        url
                    )
                    mPlayLists[PLAYLIST_Like].playList.add(newSong)
                } else {
                    var bitmap: Bitmap? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val thumb =
                                this@MainActivity.applicationContext.contentResolver.loadThumbnail(
                                    Uri.parse(url), Size(640, 480), null
                                )
                            bitmap = thumb
                        } catch (e: IOException) {
                            Log.e(MY_TAG, "can't find bitmap: ${e.message}")
                        }
                    }
                    val newSong = SongCustom(
                        id,
                        name,
                        artist,
                        duration,
                        0,
                        title,
                        "",
                        bitmap,
                        thumbnail,
                        true,
                        url
                    )
                    mPlayLists[PLAYLIST_Like].playList.add(newSong)
                }
            }
        }
        cursor.close()
        return true
    }

    private fun saveInDataBase(it: SongCustom) {
        val db = dbHelper.writableDatabase
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(SongReaderContract.SongEntry.COLUMN_ID, it.id)
            put(SongReaderContract.SongEntry.COLUMN_NAME, it.name)
            put(SongReaderContract.SongEntry.COLUMN_TITLE, it.title)
            put(SongReaderContract.SongEntry.COLUMN_ARTISTS, it.artists)
            put(SongReaderContract.SongEntry.COLUMN_DURATION, it.duration)
            put(SongReaderContract.SongEntry.COLUMN_THUMBNAIL, it.thumbnail)
            put(SongReaderContract.SongEntry.COLUMN_URL, it.linkUri)
        }

// Insert the new row, returning the primary key value of the new row
        val newRowId = db?.insert(SongReaderContract.SongEntry.TABLE_NAME, null, values)
        Log.d(MY_TAG, "insert thanh cong : $newRowId")
    }

    override fun onStart() {
        super.onStart()
        Log.d(MY_TAG, "onStart activity")
        val intentService = Intent(this, MusicControllerService::class.java)
        bindService(intentService, conn, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            addAction(ACTION_MUSIC)
        }
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getIntExtra(KEY_ACTION_MUSIC, 0)
                Log.d(MY_TAG, "onReceive activity ${intent?.action} ------------- $action")
                if (action != null)
                    handActionMusic(action)
            }
        }
        registerReceiver(br, filter)
    }

    override fun onDestroy() {
        Log.d(MY_TAG, "activity onDestroy")
        super.onDestroy()
        unregisterReceiver(br)
        unbindService(conn)
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            mPager.currentItem = mPager.currentItem - 1
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(MY_TAG, "onRequestPermissionsResult")
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Toast.makeText(this, "Allow...", Toast.LENGTH_SHORT)
                        .show()
                    loadDataStorage()
                } else {
                    //permission denied
                    Toast.makeText(this, "Storage permission required...", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        Log.e("permission", "check permission")
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun loadDataStorage() {
        Log.d(MY_TAG, "loadDataStorage")
        if (checkStoragePermission()) {
            mainViewModel.setLoading(true, PLAYLIST_STORAGE)
            GlobalScope.launch(Dispatchers.Main) {
                val resultOK = async(Dispatchers.IO) {
                    return@async mPlayLists[PLAYLIST_STORAGE].getListFromStorage(this@MainActivity)
                }
                if (resultOK.await()) {
                    mainViewModel.readData(mPlayLists[PLAYLIST_STORAGE].playList, PLAYLIST_STORAGE)
                    mainViewModel.setLoading(false, PLAYLIST_STORAGE)
                } else mainViewModel.setLoading(false, PLAYLIST_STORAGE)
            }
        } else requestStoragePermission()
    }

    private fun searchSong(name: String) {// get data with key
        mainViewModel.setLoading(true, PLAYLIST_SEARCH)
        val call = apiSearchService.getCurrentData(typeSearch, num, name)
        call.enqueue(object : Callback<DataSearchResult> {
            override fun onResponse(
                call: Call<DataSearchResult>,
                response: Response<DataSearchResult>
            ) {
                if (response.code() == 200) {
                    mPlayLists[PLAYLIST_SEARCH].playList.clear()
                    try {
                        if (response.body() != null) {
                            if (response.body()?.data != null) {
                                var dataResponse: DatumSearch? = null
                                try {
                                    dataResponse = response.body()!!.data[0]
                                } catch (e: Exception) {
                                    Log.e(MY_TAG, "error get data search: ${e.message}")
                                }
                                //load du lieu
                                var count = 0
                                dataResponse?.song?.forEach {
                                    count++
                                    if (count > 20) return@forEach
                                    var thumb: String? =
                                        "https://photo-resize-zmp3.zadn.vn/w320_r1x1_png/${it.thumb}"

                                    mPlayLists[PLAYLIST_SEARCH].playList.add(
                                        SongCustom(
                                            it.id,
                                            it.name,
                                            it.artist,
                                            it.duration.toInt() * 1000,
                                            it.duration.toInt(),
                                            it.name,
                                            "",
                                            null,
                                            thumb,
                                            false,
                                            "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320"
                                        )
                                    )
                                }
                            }
                        }

                        mPlayLists[PLAYLIST_SEARCH].id = PLAYLIST_SEARCH
                        mainViewModel.readData(
                            mPlayLists[PLAYLIST_SEARCH].playList,
                            mPlayLists[PLAYLIST_SEARCH].id
                        )
                    } catch (e: ExceptionInInitializerError) {
                        Log.e(MY_TAG, "${e.message}")
                    }
                } else Log.e("aaa", "response.code() = ${response.code()}")
                mainViewModel.setLoading(false, PLAYLIST_SEARCH)
            }

            override fun onFailure(call: Call<DataSearchResult>, t: Throwable) {
                Log.e(MY_TAG, "error getCurrentSongData ${t.message}")
                mainViewModel.setLoading(false, PLAYLIST_SEARCH)
            }
        })
    }

    private fun relatedSong(id: String) {// get data with key
        mainViewModel.setLoading(true, PLAYLIST_SEARCH)
        val call = apiRelatedSong.getCurrentData(id)
        call.enqueue(object : Callback<RelatedSong> {
            override fun onResponse(
                call: Call<RelatedSong>,
                response: Response<RelatedSong>
            ) {
                mainViewModel.setLoading(false, PLAYLIST_SEARCH)
                if (response.code() == 200) {
                    mPlayLists[PLAYLIST_RELATED].playList.clear()
                    mPlayLists[PLAYLIST_RELATED].id = PLAYLIST_RELATED
                    val list = response.body()?.data?.items
                    list?.forEach {
                        val songCustom = SongCustom(
                            it.id,
                            it.name,
                            it.artistsNames,
                            it.duration.toInt() * 1000,
                            280,
                            it.title,
                            "",
                            null,
                            it.thumbnail,
                            false,
                            "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320"
                        )
                        Log.d(MY_TAG, "add = $songCustom\n")
                        mPlayLists[PLAYLIST_RELATED].playList.add(songCustom)
                    }
                    mainViewModel.readData(mPlayLists[PLAYLIST_RELATED].playList, PLAYLIST_RELATED)
                } else Log.e(MY_TAG, "response.code() = ${response.code()}")
            }

            override fun onFailure(call: Call<RelatedSong>, t: Throwable) {
                Log.e(MY_TAG, "error getCurrentSongData ${t.message}")
                mainViewModel.setLoading(false, PLAYLIST_RELATED)
            }
        })
    }

    private fun loadDataDataChart() {
        if (checkInternet()) {
            getListFromApiChart()
        } else Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show()
    }

    private fun getListFromApiChart() {
        mainViewModel.setLoading(true, PLAYLIST_CHART)
        Log.d(MY_TAG, "getCurrentChartData")
        val call = serviceApiGetChart.getCurrentData(
            songId,
            videoId,
            albumId,
            chart,
            time
        )
        call.enqueue(object : Callback<DataChartResult> {
            override fun onResponse(
                call: Call<DataChartResult>,
                response: Response<DataChartResult>
            ) {
                if (response.code() == 200) {
                    Log.d(MY_TAG, "response.code() == 200")
                    mPlayLists[PLAYLIST_CHART].playList.clear()
                    val dataResponse = response.body()!!
                    val listChart = dataResponse.data
                    listChart.song.forEach {
                        // Load thumbnail of a specific media item.
                        val thumbnail = it.thumbnail
                        mPlayLists[PLAYLIST_CHART].playList.add(
                            SongCustom(
                                it.id,
                                it.name,
                                it.artistsNames,
                                it.duration.toInt() * 1000,
                                it.duration.toInt(),
                                it.title,
                                "",
                                null,
                                thumbnail,
                                false,
                                "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320"
                            )
                        )
                    }
                    mPlayLists[PLAYLIST_CHART].id = PLAYLIST_CHART
                    mainViewModel.readData(
                        mPlayLists[PLAYLIST_CHART].playList,
                        mPlayLists[PLAYLIST_CHART].id
                    )
                    Log.d(MY_TAG, "loadDataChartList success ")
                } else Log.e("aaa", "response.code() = ${response.code()}")
                mainViewModel.setLoading(false, PLAYLIST_CHART)
            }

            override fun onFailure(call: Call<DataChartResult>, t: Throwable) {
                Log.e(MY_TAG, "error getCurrentChartData ${t.message}")
                mainViewModel.setLoading(false, PLAYLIST_CHART)
            }
        })
    }

    private fun checkInternet(): Boolean {
        return true
    }

    private fun handBtnPlayPause() {
        if (mBound) {
            if (mService.isPng() == true) {
                controlMusic(ACTION_PAUSE)
                mainViewModel.handPause()
            } else {
                controlMusic(ACTION_RESUME)
                mainViewModel.handResume()
            }
        }
    }

    private fun controlMusic(action: Int) {
        val intent = Intent().also {
            it.action = ACTION_MUSIC
            it.putExtra(KEY_ACTION_MUSIC, action)
        }
        sendBroadcast(intent)
    }

    private fun handActionMusic(action: Int) {
        when (action) {
            ACTION_RESUME -> {
                mainViewModel.handResume()
            }
            ACTION_PAUSE -> {
                mainViewModel.handPause()
            }
            ACTION_STOP -> {
                Log.d(MY_TAG, "stop bound service")
                if (mBound)
                    unbindService(conn)
                mainViewModel.handStop()
                mainViewModel.listPos
                this.finish()
            }
            ACTION_PLAY, ACTION_NEXT, ACTION_BACK -> {
                if (!mBound) {
                    val intentService = Intent(this, MusicControllerService::class.java)
                    bindService(intentService, conn, Context.BIND_AUTO_CREATE)
                }
                if (mService.songs[mService.songPos].thumbnail != null)
                    Glide.with(this).load(mService.songs[mService.songPos].thumbnail)
                        .into(binding.imgSong)
                mainViewModel.initItemPlaying(
                    mService.songs[mService.songPos].bitmap,
                    mService.songs[mService.songPos].title,
                    mService.songs[mService.songPos].artists, true
                )
            }
            ACTION_PLAY_PAUSE -> {
                if (mService.isPng() == true) handActionMusic(ACTION_RESUME)
                else handActionMusic(ACTION_PAUSE)
            }
        }
    }

    private fun downLoad(item: SongCustom) {
        var dir = Environment.DIRECTORY_MUSIC
        dir += "/klp"
        val fileDir = File(dir)
        if (!fileDir.isDirectory) {
            fileDir.mkdir()
        }
        Toast.makeText(
            this, "Download song " + item.title,
            Toast.LENGTH_SHORT
        ).show()
        // Download File
        // Download File
        val request = DownloadManager.Request(
            Uri.parse(item.linkUri)
        )
        request.setDescription(item.name)
        request.setTitle(item.title)
        // in order for this if to run, you must use the android 3.2 to
        // compile your app
        // in order for this if to run, you must use the android 3.2 to
        // compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        request.setDestinationInExternalPublicDir(dir, "nameFile.mp3")

        // get download service and enqueue file

        // get download service and enqueue file
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    companion object {
        //http://mp3.zing.vn/xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1
        val serviceApiGetChart by lazy { ApiChartService.create() }
        var songId = 0
        var videoId = 0
        var albumId = 0
        var chart = "song"
        var time = -1

        ////http://ac.mp3.zing.vn/complete?type=artist,song,key,code&num=500&query=Anh Thế Giới Và Em
        val apiSearchService by lazy { ApiSearchService.create() }
        var typeSearch = "artist,song,key,code"
        var num = 500.toLong()

        //http://mp3.zing.vn/xhr/recommend?type=audio&id=ZW67OIA0
        val apiRelatedSong by lazy {
            ApiRelatedSong.create()
        }
    }
}