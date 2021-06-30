package com.loan555.musicplayer

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.loan555.musicplayer.databinding.ActivityMainBinding
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.SongList
import com.loan555.musicplayer.service.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

const val NUM_PAGES = 3
const val MY_TAG = "aaa"
const val STORAGE_REQUEST_CODE = 1

const val PLAYLIST_STORAGE = 0
const val PLAYLIST_CHART = 1
const val PLAYLIST_RELATED = 2
const val PLAYLIST_LIKE = 3

class MainActivity : AppCompatActivity() {
    //broadcast
    private lateinit var br: BroadcastReceiver

    //permission
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    //view
    private lateinit var mPager: ViewPager
    private lateinit var mainViewModel: AppViewModel
    private lateinit var binding: ActivityMainBinding

    //service
    companion object {
        lateinit var mService: MusicControllerService
        var mBound: Boolean = false
        var mSongList = SongList()// list storage
        var mSongListChart = SongList()// list chart
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val conn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.d("aaa", "onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicControllerService.MusicControllerBinder
            mService = binder.getService()
            mBound = true
            /** load dữ liệu tại đây  */// hamf nayf hiện đang ko nhận giá trị bởi api và view bất đồng bộ
            loadDataStorage()
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
        //tool bar
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
        mainViewModel.listPos.observe(this, {
            when (it) {

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
        loadDataChart()
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
            GlobalScope.launch(Dispatchers.Main) {
                val resultOK = async(Dispatchers.IO) {
                    return@async mSongList.getListFromStorage(this@MainActivity)
                }
                if (resultOK.await()) {
                    mainViewModel.readData(mSongList.playList, PLAYLIST_STORAGE)
                }
            }
        } else requestStoragePermission()
    }

    private fun loadDataChart() {
        if (checkInternet()) {
            Log.d(MY_TAG,"loadDataChart")
            GlobalScope.launch(Dispatchers.Main) {
                val resultOK = async(Dispatchers.IO) {
                    return@async mSongListChart.getListFromApiChart(this@MainActivity)
                }
                if (resultOK.await()) {
                    mainViewModel.readData(mSongListChart.playList, PLAYLIST_CHART)
                    Log.d(MY_TAG,"list chart = ${mainViewModel.mListSongChartLiveData.value}")
                }else
                    Log.d(MY_TAG,"can't read chart")
            }
        } else Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show()
    }

    private fun checkInternet(): Boolean {
        return true
    }

    private fun handViewItem() {
        val item = mService.songs[mService.songPos]
        val isPlaying = mService.isPng() == true
        mainViewModel.initItemPlaying(item.bitmap, item.title, item.artists, isPlaying)
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
}