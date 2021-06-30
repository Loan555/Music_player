package com.loan555.musicplayer.ui.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loan555.musicplayer.MY_TAG
import com.loan555.musicplayer.MainActivity
import com.loan555.musicplayer.databinding.FragmentHomeBinding
import com.loan555.musicplayer.model.AppViewModel
import com.loan555.musicplayer.model.SongCustom
import java.lang.Exception

class HomeFragment : Fragment(), ListSongAdapter.OnItemClickListener {

    private lateinit var songAdapter: ListSongAdapter
    private lateinit var homeViewModel: AppViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // khai báo view binding
        val recyclerView: RecyclerView = binding.recyclerSongs

        recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        homeViewModel.mListSongLiveData.observe(viewLifecycleOwner, Observer {
            // gán binding cho textView để nó theo dõi biến _text
            songAdapter = ListSongAdapter(it, this)
            recyclerView.adapter = songAdapter
        })
        Log.d(MY_TAG, "bind viewModel HomeFragment")
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(v: View?, item: SongCustom, position: Int) {
        if (MainActivity.mService.listPlaying != MainActivity.mSongList.listID) {// neu list playing in service is != list offline
            MainActivity.mService.songs = MainActivity.mSongList.playList
            Log.d(MY_TAG, "list playing is: storage")
        } else MainActivity.mService.songs = MainActivity.mSongList.playList
        MainActivity.mService.player?.setOnCompletionListener {
            MainActivity.mService.playNext()
            homeViewModel.initItemPlaying(
                MainActivity.mService.songs[MainActivity.mService.songPos].bitmap,
                MainActivity.mService.songs[MainActivity.mService.songPos].title,
                MainActivity.mService.songs[MainActivity.mService.songPos].artists,
                MainActivity.mService.isPng() == true
            )
        }
        MainActivity.mService.playSong(position)
        homeViewModel.initItemPlaying(
            item.bitmap,
            item.title,
            item.artists,
            MainActivity.mService.isPng() == true
        )
    }

    /**
     * Data Storage
     */

    private fun checkPermissionStorage(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }
}