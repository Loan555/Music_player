package com.loan555.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
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
import com.loan555.musicplayer.model.SongCustom
import com.loan555.musicplayer.service.MusicControllerService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

const val NUM_PAGES = 3
const val MY_TAG = "aaa"
const val STORAGE_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {
    //permission
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    //view
    private lateinit var mPager: ViewPager
    private lateinit var mainViewModel: AppViewModel
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    //service
    private lateinit var mService: MusicControllerService
    private var mBound: Boolean = false

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
        setContentView(R.layout.activity_main)

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

        pager_main.setOnClickListener {
            val song = SongCustom(
                "id",
                "tên bài hát̃",
                "tên ca sĩ", 100, 12, "tên hiển thị", "tên albums", true, "link ủi"
            )
            mainViewModel.addData(song)
            Log.d(MY_TAG,"click")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(MY_TAG, "onStart activity")
        Intent(this, MusicControllerService::class.java).also {
            bindService(it, conn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        Log.d(MY_TAG,"activity onDestroy")
        super.onDestroy()
        mBound = false
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
                    if (mBound)
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
        if (checkStoragePermission()) {
            GlobalScope.launch(Dispatchers.Main) {
                val resultOK = async(Dispatchers.IO) {
                    return@async mService.getListFromStorage()
                }
                if (resultOK.await()) {
                    for (i in 0 until mService.songsStorage.size){
                        mainViewModel.addData(mService.songsStorage[i])
                    }
                }
            }
        } else requestStoragePermission()
    }
}