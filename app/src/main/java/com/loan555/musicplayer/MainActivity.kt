package com.loan555.musicplayer

import android.Manifest
import android.R.attr.name
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.loan555.musicplayer.databinding.ActivityMainBinding
import com.loan555.musicplayer.model.*
import com.loan555.musicplayer.service.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


const val NUM_PAGES = 3
const val MY_TAG = "aaa"
const val STORAGE_REQUEST_CODE = 1


const val PLAYLIST_NOTHING = -1
const val PLAYLIST_STORAGE = 0
const val PLAYLIST_CHART = 1
const val PLAYLIST_SEARCH = 2
const val PLAYLIST_LIKE = 3

class MainActivity : AppCompatActivity() {
    private val dbHelper = SongReaderDbHelper(this)

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
    private val mPlayListRelated = PlayList()
    private val mPlayLists = ArrayList<PlayList>()// khoi tao mot danh sacsh cacs playList rong

    /** Defines callbacks for service binding, passed to bindService()  */
    private val conn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.d("aaa", "onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicControllerService.MusicControllerBinder
            mService = binder.getService()
            mBound = true
            mainViewModel.listPos.observe(
                this@MainActivity,
                {// khi list có thay đổ thì phải load lại data vào listPlaying trong service
                    if (mBound)
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
                        }
                })
            mainViewModel.songPos.observe(this@MainActivity, {
                Log.d(MY_TAG, "click new posision song")
                if (it >= 0 && it < mService.songs.size)
                    mService.playSong(it)
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
                    1 -> {
                        nav_view.menu.findItem(R.id.navigation_chart).isChecked = true
                        supportActionBar?.setTitle(R.string.title_dashboard)
                    }
                    2 -> {
                        nav_view.menu.findItem(R.id.navigation_search).isChecked = true
                        supportActionBar?.setTitle(R.string.title_notifications)
                    }
                    else -> {
                        nav_view.menu.findItem(R.id.navigation_offline).isChecked = true
                        supportActionBar?.setTitle(R.string.title_home)
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
        mainViewModel.textSearch.observe(this, {
            searchSong(it)
        })
        mainViewModel.pageLoader.observe(this, {
            when (it) {
                -1 -> {
                }
                0 -> {
                    loadDataStorage()
                }
                1 -> {
                    loadDataDataChart()
                }
                2 -> {
                    mainViewModel.textSearch.value?.let { it1 -> searchSong(it1) }
                }
            }
        })
        mainViewModel.songDownload.observe(this, {
            if (it != null) {
                downLoad(it)
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
        /**
         * load data
         */
        mPlayLists.add(mPlayListStorage)
        mPlayLists.add(mPlayListChart)
        mPlayLists.add(mPlayListRelated)
        loadDataStorage()
        loadDataDataChart()
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
                                    var bitmap: Bitmap? = null
                                    mPlayLists[PLAYLIST_SEARCH].playList.add(
                                        SongCustom(
                                            it.id,
                                            it.name,
                                            it.artist,
                                            it.duration.toInt() * 1000,
                                            it.duration.toInt(),
                                            it.name,
                                            "",
                                            bitmap,
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
                        val thumbnail = null
                        mPlayLists[PLAYLIST_CHART].playList.add(
                            SongCustom(
                                it.id,
                                it.name,
                                it.artistsNames,
                                it.duration.toInt() * 1000,
                                it.duration.toInt(),
                                it.title,
                                "it.album.toString()",
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
            }
            ACTION_PLAY, ACTION_NEXT, ACTION_BACK -> {
                if (!mBound) {
                    val intentService = Intent(this, MusicControllerService::class.java)
                    bindService(intentService, conn, Context.BIND_AUTO_CREATE)
                }
                mainViewModel.initItemPlaying(
                    mService.songs[mService.songPos].bitmap, mService.songs[mService.songPos].title,
                    mService.songs[mService.songPos].artists, mService.isPng() == true
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